"""OpenClaw Message Bridge — forwards messages to/from the agent inside E2B sandboxes.

The bridge handles two modes:
1. **Direct mode** (no sandbox / E2B not configured): Falls through to the existing
   direct Anthropic streaming in llm.py. This keeps the app working without E2B.
2. **Sandbox mode** (E2B configured): Creates/resumes a sandbox, executes user code
   or forwards the prompt to an OpenClaw agent process inside the sandbox.

The bridge produces SSE events matching our existing contract (token, tool_start,
tool_result, file, error, done).
"""

from __future__ import annotations

import json
import logging
from collections.abc import AsyncGenerator

from app.config import get_settings
from app.models.schemas import SSEEvent, SSEEventType
from app.services.sandbox import get_sandbox_manager, SandboxState
from app.services.llm import stream_chat as direct_stream_chat, stream_chat_with_tools, estimate_credits
from app.services.tools import get_tool_schemas, execute_tool

logger = logging.getLogger(__name__)


def is_sandbox_enabled() -> bool:
    """Check if E2B is configured (non-empty API key)."""
    return bool(get_settings().e2b_api_key)


async def stream_agent_response(
    user_id: str,
    conversation_id: str,
    messages: list[dict],
    model: str,
    system_prompt: str = "You are a helpful AI assistant powered by OpenClaw.",
    api_key: str | None = None,
) -> AsyncGenerator[str, None]:
    """Stream an agent response — via sandbox if E2B is configured, else direct LLM.

    This is the single entry point that the chat endpoint calls. It decides
    whether to use sandbox execution or fall through to direct Anthropic streaming.
    """
    if not is_sandbox_enabled():
        # No sandbox: use tool calling with server-side tool execution
        async for sse_line in stream_chat_with_tools(
            messages=messages,
            model=model,
            tools=get_tool_schemas(),
            tool_executor=execute_tool,
            conversation_id=conversation_id,
            user_id=user_id,
            system_prompt=system_prompt,
            api_key=api_key,
        ):
            yield sse_line
        return

    # Sandbox mode: get or create sandbox, then run agent
    manager = get_sandbox_manager()

    try:
        info = await manager.get_or_create(
            user_id=user_id,
            conversation_id=conversation_id,
            anthropic_api_key=api_key,
            model=model,
        )
    except Exception as e:
        logger.exception("Failed to create/resume sandbox for conversation %s", conversation_id)
        error_event = SSEEvent(type=SSEEventType.error, error=f"Sandbox error: {e}")
        yield f"data: {error_event.model_dump_json()}\n\n"
        # Fallback to tool calling without sandbox
        async for sse_line in stream_chat_with_tools(
            messages=messages, model=model, tools=get_tool_schemas(),
            tool_executor=execute_tool, conversation_id=conversation_id,
            user_id=user_id, system_prompt=system_prompt, api_key=api_key,
        ):
            yield sse_line
        return

    # Stream from sandbox agent
    async for sse_line in _stream_from_sandbox(
        info=info,
        messages=messages,
        model=model,
        system_prompt=system_prompt,
    ):
        yield sse_line


async def _stream_from_sandbox(
    info,
    messages: list[dict],
    model: str,
    system_prompt: str,
) -> AsyncGenerator[str, None]:
    """Execute the user's message inside the sandbox with tool calling.

    Uses the full tool-calling loop with code execution routed to the sandbox.
    When OpenClaw is installed in the sandbox image, this will forward to the
    OpenClaw WebSocket gateway on port 18789 instead.
    """
    async for sse_line in stream_chat_with_tools(
        messages=messages,
        model=model,
        tools=get_tool_schemas(),
        tool_executor=execute_tool,
        conversation_id=info.conversation_id,
        user_id=info.user_id,
        system_prompt=system_prompt,
    ):
        yield sse_line
