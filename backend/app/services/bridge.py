"""OpenClaw Message Bridge — forwards messages to/from the agent inside E2B sandboxes.

The bridge handles two modes:
1. **Direct mode** (no sandbox / E2B not configured): Falls through to the existing
   direct Anthropic streaming in llm.py. This keeps the app working without E2B.
2. **Sandbox mode** (E2B configured): Creates/resumes a sandbox, executes user code
   or forwards the prompt to an OpenClaw agent process inside the sandbox via WebSocket.

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
from app.services.llm import stream_chat_with_tools
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
    """Connect to the OpenClaw Gateway WebSocket in the sandbox and stream events.

    OpenClaw Gateway Protocol:
    1. Connect to wss://{sandbox_id}-18789.e2b.dev/ws
    2. Send connect frame: {type:"req", method:"connect", role:"operator", ...}
    3. Receive connect ack: {type:"res", ok:true, ...}
    4. Send chat message: {type:"event", event:"chat.send", payload:{text:"..."}}
    5. Receive streamed events: thinking, tool_start, tool_result, message, done

    Falls back to direct LLM tool-calling if WebSocket fails.
    """
    import asyncio as _asyncio
    import uuid
    import websockets
    import websockets.exceptions

    ws_url = f"wss://{info.sandbox_id}-18789.e2b.dev/ws"

    try:
        async with websockets.connect(ws_url, open_timeout=10) as ws:
            # Step 1: Send connect frame (operator role)
            connect_id = str(uuid.uuid4())[:8]
            await ws.send(json.dumps({
                "type": "req",
                "id": connect_id,
                "method": "connect",
                "params": {
                    "minProtocol": 1,
                    "maxProtocol": 1,
                    "role": "operator",
                    "client": {
                        "name": "myopenclaw-backend",
                        "version": "1.0.0",
                    },
                }
            }))

            # Step 2: Wait for connect ack
            ack_raw = await _asyncio.wait_for(ws.recv(), timeout=5)
            ack = json.loads(ack_raw)
            if ack.get("type") != "res" or not ack.get("ok"):
                logger.warning("OpenClaw connect failed: %s", ack)
                async for sse_line in _fallback_to_direct(info, messages, model, system_prompt):
                    yield sse_line
                return

            # Step 3: Send the user's latest message
            last_user_msg = ""
            for m in reversed(messages):
                if m.get("role") == "user":
                    last_user_msg = m.get("content", "")
                    break

            await ws.send(json.dumps({
                "type": "event",
                "event": "chat.send",
                "payload": {
                    "text": last_user_msg,
                    "session": "main",
                }
            }))

            # Step 4: Stream events until done
            async for raw in ws:
                event = json.loads(raw)
                sse = _map_openclaw_event(event)
                if sse:
                    yield f"data: {sse.model_dump_json()}\n\n"

                etype = event.get("event", event.get("type", ""))
                if etype in ("agent.done", "chat.done", "done"):
                    break

    except (
        websockets.exceptions.WebSocketException,
        OSError,
        TimeoutError,
        _asyncio.TimeoutError,
    ) as e:
        logger.warning(
            "WebSocket to sandbox %s failed (%s), falling back to direct tool loop",
            info.sandbox_id, e,
        )
        async for sse_line in _fallback_to_direct(info, messages, model, system_prompt):
            yield sse_line


async def _fallback_to_direct(
    info, messages: list[dict], model: str, system_prompt: str
) -> AsyncGenerator[str, None]:
    """Fall back to server-side tool calling when WebSocket fails."""
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


def _map_openclaw_event(event: dict) -> SSEEvent | None:
    """Translate an OpenClaw Gateway WebSocket event to our SSEEvent schema.

    OpenClaw emits events with an "event" field (e.g., "chat.message",
    "agent.thinking", "agent.tool_start") and a "payload" dict.
    """
    etype = event.get("event", event.get("type", ""))
    payload = event.get("payload", event)

    # Text streaming
    if etype in ("chat.message", "message", "agent.message"):
        content = payload.get("text", payload.get("content", ""))
        if content:
            return SSEEvent(type=SSEEventType.token, content=content)

    # Agent thinking / streaming tokens
    if etype in ("agent.thinking", "agent.stream", "thinking"):
        content = payload.get("text", payload.get("content", ""))
        if content:
            return SSEEvent(type=SSEEventType.token, content=content)

    # Tool start
    if etype in ("agent.tool_start", "action", "tool_start"):
        tool_name = payload.get(
            "tool", payload.get("action", payload.get("name", "unknown"))
        )
        return SSEEvent(
            type=SSEEventType.tool_start,
            tool=tool_name,
            input=payload.get("args", payload.get("input", {})),
        )

    # Tool result / observation
    if etype in ("agent.tool_result", "observation", "tool_result"):
        tool_name = payload.get("tool", payload.get("name", "unknown"))
        output = str(
            payload.get("output", payload.get("content", payload.get("result", "")))
        )
        return SSEEvent(
            type=SSEEventType.tool_result,
            tool=tool_name,
            output=output[:2000],
        )

    # File created
    if etype in ("file.created", "file_created", "file"):
        return SSEEvent(
            type=SSEEventType.file,
            filename=payload.get("filename", payload.get("name", "")),
            url=payload.get("url", ""),
            size=payload.get("size", 0),
        )

    # Error
    if etype in ("error", "agent.error"):
        return SSEEvent(
            type=SSEEventType.error,
            error=payload.get("message", payload.get("error", str(payload))),
        )

    # Done
    if etype in ("agent.done", "chat.done", "done"):
        return SSEEvent(
            type=SSEEventType.done,
            total_tokens=payload.get("total_tokens", 0),
            credits_used=payload.get("credits_used", 0),
        )

    return None
