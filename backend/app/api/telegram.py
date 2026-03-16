"""Telegram webhook handler.

Flow:
  incoming update → look up / create user by Telegram chat_id
  → look up / create conversation → run agent → reply via Telegram Bot API

Register with @BotFather, then set the webhook URL to:
  POST https://your-domain.com/webhooks/telegram
"""

from __future__ import annotations

import logging

import httpx
from fastapi import APIRouter, HTTPException, Request

from app.config import get_settings
from app.db.messaging_users import get_or_create_messaging_user, get_or_create_conversation_for_user
from app.services.bridge import stream_agent_response

logger = logging.getLogger(__name__)

router = APIRouter()


@router.post("/webhooks/telegram")
async def telegram_webhook(request: Request):
    """Receive Telegram updates and route them to the agent."""
    settings = get_settings()
    if not settings.telegram_bot_token:
        raise HTTPException(status_code=503, detail="Telegram not configured")

    update = await request.json()
    message = update.get("message") or update.get("edited_message")
    if not message:
        return {"ok": True}  # Ignore non-message updates (e.g. channel posts)

    chat_id = str(message["chat"]["id"])
    from_user = message.get("from", {})
    platform_user_id = str(from_user.get("id", chat_id))

    # Resolve text — handle voice notes via transcription
    text = message.get("text") or message.get("caption", "")
    if not text and message.get("voice"):
        text = await _transcribe_voice(message["voice"], settings.telegram_bot_token)
    if not text:
        return {"ok": True}  # Nothing to process

    # Map Telegram user → Supabase user + conversation
    user_id = await get_or_create_messaging_user(
        platform="telegram",
        platform_user_id=platform_user_id,
        display_name=_display_name(from_user),
    )
    conversation_id = await get_or_create_conversation_for_user(
        user_id=user_id,
        platform="telegram",
        platform_user_id=platform_user_id,
    )

    # Run agent and accumulate the full response
    reply_text = await _run_agent_collect(
        user_id=user_id,
        conversation_id=conversation_id,
        text=text,
    )

    await _send_telegram_message(chat_id, reply_text, settings.telegram_bot_token)
    return {"ok": True}


async def _transcribe_voice(voice: dict, bot_token: str) -> str:
    """Download a Telegram voice OGG and transcribe via Whisper."""
    from app.services.transcription import transcribe_audio

    file_id = voice["file_id"]
    async with httpx.AsyncClient(timeout=30) as client:
        resp = await client.get(
            f"https://api.telegram.org/bot{bot_token}/getFile",
            params={"file_id": file_id},
        )
        if resp.status_code != 200:
            return ""
        file_path = resp.json()["result"]["file_path"]

        audio_resp = await client.get(
            f"https://api.telegram.org/file/bot{bot_token}/{file_path}"
        )
        if audio_resp.status_code != 200:
            return ""
        audio_bytes = audio_resp.content

    return await transcribe_audio(audio_bytes, filename="voice.ogg")


async def _run_agent_collect(user_id: str, conversation_id: str, text: str) -> str:
    """Run the agent and collect all token chunks into a single reply string."""
    import json

    tokens: list[str] = []
    messages = [{"role": "user", "content": text}]

    async for sse_line in stream_agent_response(
        user_id=user_id,
        conversation_id=conversation_id,
        messages=messages,
        model=get_settings().sonnet_model,
    ):
        if not sse_line.startswith("data: "):
            continue
        try:
            event = json.loads(sse_line[6:])
            if event.get("type") == "token" and event.get("content"):
                tokens.append(event["content"])
        except (json.JSONDecodeError, KeyError):
            continue

    return "".join(tokens) or "(no response)"


async def _send_telegram_message(chat_id: str, text: str, bot_token: str):
    """Send a text reply to a Telegram chat, splitting long messages."""
    max_len = 4096
    chunks = [text[i:i + max_len] for i in range(0, len(text), max_len)]
    async with httpx.AsyncClient(timeout=15) as client:
        for chunk in chunks:
            await client.post(
                f"https://api.telegram.org/bot{bot_token}/sendMessage",
                json={"chat_id": chat_id, "text": chunk, "parse_mode": "Markdown"},
            )


def _display_name(from_user: dict) -> str:
    first = from_user.get("first_name", "")
    last = from_user.get("last_name", "")
    username = from_user.get("username", "")
    return (f"{first} {last}".strip() or username or "Telegram User")
