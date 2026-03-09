"""Anthropic LLM service — handles model routing, streaming, and token counting."""

from __future__ import annotations

import json
from collections.abc import AsyncGenerator

import anthropic

from app.config import get_settings
from app.models.schemas import SSEEvent, SSEEventType


# Cost per million tokens (USD) — used for usage tracking
MODEL_COSTS = {
    "claude-haiku-4-5-20250315": {"input": 1.00, "output": 5.00},
    "claude-sonnet-4-20250514": {"input": 3.00, "output": 15.00},
    "claude-opus-4-20250514": {"input": 15.00, "output": 75.00},
}


def _get_client(api_key: str | None = None) -> anthropic.Anthropic:
    """Create Anthropic client with bundled or BYOK key."""
    key = api_key or get_settings().anthropic_api_key
    return anthropic.Anthropic(api_key=key)


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
        with client.messages.stream(
            model=model,
            max_tokens=4096,
            system=system_prompt,
            messages=messages,
        ) as stream:
            for event in stream:
                if event.type == "content_block_delta":
                    if hasattr(event.delta, "text"):
                        sse = SSEEvent(
                            type=SSEEventType.token,
                            content=event.delta.text,
                            model=model,
                        )
                        yield f"data: {sse.model_dump_json()}\n\n"

            # Final usage from the stream
            final_message = stream.get_final_message()
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
