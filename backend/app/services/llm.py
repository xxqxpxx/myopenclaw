"""Anthropic LLM service — handles model routing, streaming, and tool calling."""

from __future__ import annotations

import json
import logging
from collections.abc import AsyncGenerator
from typing import Any

import anthropic

from app.config import get_settings
from app.models.schemas import SSEEvent, SSEEventType

logger = logging.getLogger(__name__)


# Cost per million tokens (USD) — used for usage tracking
MODEL_COSTS = {
    "claude-3-5-haiku-20241022": {"input": 1.00, "output": 5.00},
    "claude-sonnet-4-20250514": {"input": 3.00, "output": 15.00},
    "claude-opus-4-20250514": {"input": 15.00, "output": 75.00},
}


def _get_client(api_key: str | None = None) -> anthropic.AsyncAnthropic:
    """Create async Anthropic client with bundled or BYOK key."""
    key = api_key or get_settings().anthropic_api_key
    return anthropic.AsyncAnthropic(api_key=key)


def route_model(content: str, user_model: str | None = None) -> str:
    """Pick the best model based on query complexity.

    - Short simple queries → Haiku (cheap, fast)
    - Analysis/writing/code → Sonnet
    - User override → whatever they ask for
    """
    if user_model:
        return user_model

    settings = get_settings()
    token_estimate = len(content.split())

    # Simple heuristic: short messages without code indicators → Haiku
    code_indicators = ["```", "def ", "function ", "class ", "import ", "write code", "script"]
    complex_indicators = ["analyze", "explain", "compare", "summarize", "review", "refactor"]

    has_code = any(ind in content.lower() for ind in code_indicators)
    has_complex = any(ind in content.lower() for ind in complex_indicators)

    if token_estimate < 30 and not has_code and not has_complex:
        return settings.haiku_model
    elif has_code or has_complex or token_estimate > 200:
        return settings.sonnet_model
    else:
        return settings.haiku_model


def calculate_cost(model: str, input_tokens: int, output_tokens: int) -> float:
    """Calculate USD cost for a request."""
    costs = MODEL_COSTS.get(model, {"input": 3.0, "output": 15.0})
    return (input_tokens * costs["input"] / 1_000_000) + (
        output_tokens * costs["output"] / 1_000_000
    )


def estimate_credits(model: str, input_tokens: int, output_tokens: int) -> int:
    """Convert token usage to credit cost (1 credit ≈ $0.001)."""
    cost = calculate_cost(model, input_tokens, output_tokens)
    return max(1, round(cost * 1000))


async def stream_chat(
    messages: list[dict],
    model: str,
    system_prompt: str = "You are a helpful AI assistant.",
    api_key: str | None = None,
) -> AsyncGenerator[str, None]:
    """Stream a chat completion as SSE-formatted lines.

    Yields `data: {json}\n\n` strings ready to be sent over SSE.
    The final event is always type=done with token counts.
    """
    client = _get_client(api_key)
    input_tokens = 0
    output_tokens = 0

    try:
        async with client.messages.stream(
            model=model,
            max_tokens=4096,
            system=system_prompt,
            messages=messages,
        ) as stream:
            async for event in stream:
                if event.type == "content_block_delta":
                    if hasattr(event.delta, "text"):
                        sse = SSEEvent(
                            type=SSEEventType.token,
                            content=event.delta.text,
                            model=model,
                        )
                        yield f"data: {sse.model_dump_json()}\n\n"

            # Final usage from the stream
            final_message = await stream.get_final_message()
            input_tokens = final_message.usage.input_tokens
            output_tokens = final_message.usage.output_tokens

    except anthropic.APIStatusError as e:
        error_event = SSEEvent(type=SSEEventType.error, error=str(e.message))
        yield f"data: {error_event.model_dump_json()}\n\n"
        return

    credits_used = estimate_credits(model, input_tokens, output_tokens)
    done_event = SSEEvent(
        type=SSEEventType.done,
        total_tokens=input_tokens + output_tokens,
        credits_used=credits_used,
        model=model,
    )
    yield f"data: {done_event.model_dump_json()}\n\n"


async def stream_chat_with_tools(
    messages: list[dict],
    model: str,
    tools: list[dict[str, Any]],
    tool_executor,
    conversation_id: str,
    user_id: str,
    system_prompt: str = "You are a helpful AI assistant powered by OpenClaw. You have access to tools for code execution, web search, file operations, and HTTP requests.",
    api_key: str | None = None,
    max_tool_rounds: int = 10,
) -> AsyncGenerator[str, None]:
    """Stream a chat completion with tool calling support.

    Handles the full agentic loop:
    1. Send messages + tool schemas to Claude
    2. Stream text tokens as SSE events
    3. When Claude makes a tool call, emit tool_start, execute, emit tool_result
    4. Feed tool results back to Claude and continue streaming
    5. Repeat until Claude produces a final text response (up to max_tool_rounds)
    """
    client = _get_client(api_key)
    total_input_tokens = 0
    total_output_tokens = 0
    current_messages = list(messages)

    for round_num in range(max_tool_rounds):
        try:
            async with client.messages.stream(
                model=model,
                max_tokens=4096,
                system=system_prompt,
                messages=current_messages,
                tools=tools,
            ) as stream:
                text_parts: list[str] = []
                tool_calls: list[dict] = []
                current_tool_name: str | None = None
                current_tool_id: str | None = None
                current_tool_input_json = ""

                async for event in stream:
                    if event.type == "content_block_start":
                        if hasattr(event.content_block, "type"):
                            if event.content_block.type == "tool_use":
                                current_tool_name = event.content_block.name
                                current_tool_id = event.content_block.id
                                current_tool_input_json = ""
                                sse = SSEEvent(
                                    type=SSEEventType.tool_start,
                                    tool=current_tool_name,
                                )
                                yield f"data: {sse.model_dump_json()}\n\n"

                    elif event.type == "content_block_delta":
                        if hasattr(event.delta, "text"):
                            text_parts.append(event.delta.text)
                            sse = SSEEvent(
                                type=SSEEventType.token,
                                content=event.delta.text,
                                model=model,
                            )
                            yield f"data: {sse.model_dump_json()}\n\n"

                        elif hasattr(event.delta, "partial_json"):
                            current_tool_input_json += event.delta.partial_json

                    elif event.type == "content_block_stop":
                        if current_tool_name:
                            try:
                                tool_input = json.loads(current_tool_input_json) if current_tool_input_json else {}
                            except json.JSONDecodeError:
                                tool_input = {"raw": current_tool_input_json}

                            tool_calls.append({
                                "name": current_tool_name,
                                "input": tool_input,
                                "id": current_tool_id or f"toolu_{round_num}_{current_tool_name}",
                            })
                            current_tool_name = None
                            current_tool_id = None
                            current_tool_input_json = ""

                final_message = await stream.get_final_message()
                total_input_tokens += final_message.usage.input_tokens
                total_output_tokens += final_message.usage.output_tokens

        except anthropic.APIStatusError as e:
            error_event = SSEEvent(type=SSEEventType.error, error=str(e.message))
            yield f"data: {error_event.model_dump_json()}\n\n"
            break

        if not tool_calls:
            break

        # Build assistant message with tool_use blocks
        assistant_content = []
        if text_parts:
            assistant_content.append({"type": "text", "text": "".join(text_parts)})
        for tc in tool_calls:
            assistant_content.append({
                "type": "tool_use",
                "id": tc["id"],
                "name": tc["name"],
                "input": tc["input"],
            })
        current_messages.append({"role": "assistant", "content": assistant_content})

        # Execute tools and collect results
        tool_results = []
        for tc in tool_calls:
            result = await tool_executor(
                tool_name=tc["name"],
                tool_input=tc["input"],
                conversation_id=conversation_id,
                user_id=user_id,
            )

            sse = SSEEvent(
                type=SSEEventType.tool_result,
                tool=tc["name"],
                output=result[:2000],
            )
            yield f"data: {sse.model_dump_json()}\n\n"

            tool_results.append({
                "type": "tool_result",
                "tool_use_id": tc["id"],
                "content": result,
            })

        current_messages.append({"role": "user", "content": tool_results})
        tool_calls.clear()

    # Done event
    credits_used = estimate_credits(model, total_input_tokens, total_output_tokens)
    done_event = SSEEvent(
        type=SSEEventType.done,
        total_tokens=total_input_tokens + total_output_tokens,
        credits_used=credits_used,
        model=model,
    )
    yield f"data: {done_event.model_dump_json()}\n\n"
