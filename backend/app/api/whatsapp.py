"""WhatsApp Cloud API webhook handler (Meta Graph API).

Flow:
  incoming message → verify webhook token → look up / create user
  → look up / create conversation → run agent → reply via Graph API

Set the webhook URL to:
  POST https://your-domain.com/webhooks/whatsapp
Verify token: set WHATSAPP_TOKEN in your Meta app webhook settings.
"""

from __future__ import annotations

import hashlib
import hmac
import logging

import httpx
from fastapi import APIRouter, HTTPException, Request, Query

from app.config import get_settings
from app.db.messaging_users import get_or_create_messaging_user, get_or_create_conversation_for_user
from app.services.bridge import stream_agent_response

logger = logging.getLogger(__name__)

router = APIRouter()

GRAPH_API_URL = "https://graph.facebook.com/v19.0"


@router.get("/webhooks/whatsapp")
async def whatsapp_verify(
    hub_mode: str = Query(alias="hub.mode", default=""),
    hub_verify_token: str = Query(alias="hub.verify_token", default=""),
    hub_challenge: str = Query(alias="hub.challenge", default=""),
):
    """Meta webhook verification handshake."""
    settings = get_settings()
    if hub_mode == "subscribe" and hub_verify_token == settings.whatsapp_token:
        return int(hub_challenge)
    raise HTTPException(status_code=403, detail="Verification failed")


@router.post("/webhooks/whatsapp")
async def whatsapp_webhook(request: Request):
    """Receive WhatsApp messages and route them to the agent."""
    settings = get_settings()
    if not settings.whatsapp_token:
        raise HTTPException(status_code=503, detail="WhatsApp not configured")

    body = await request.body()
    # Validate Meta payload signature (X-Hub-Signature-256 header)
    _verify_signature(body, request.headers.get("X-Hub-Signature-256", ""), settings.whatsapp_token)

    payload = await request.json()

    for entry in payload.get("entry", []):
        for change in entry.get("changes", []):
            value = change.get("value", {})
            for message in value.get("messages", []):
                await _handle_message(message, value, settings)

    return {"status": "ok"}


async def _handle_message(message: dict, value: dict, settings):
    """Process a single incoming WhatsApp message."""
    msg_type = message.get("type", "")
    from_number = message.get("from", "")  # WhatsApp phone number (e.g. "1234567890")

    text = ""
    if msg_type == "text":
        text = message.get("text", {}).get("body", "")
    elif msg_type == "audio":
        text = await _transcribe_whatsapp_audio(message.get("audio", {}), settings)
    elif msg_type == "image" and message.get("image", {}).get("caption"):
        text = message["image"]["caption"]

    if not text:
        return

    # Resolve display name from contacts list
    contacts = value.get("contacts", [])
    display_name = contacts[0].get("profile", {}).get("name", "WhatsApp User") if contacts else "WhatsApp User"

    user_id = await get_or_create_messaging_user(
        platform="whatsapp",
        platform_user_id=from_number,
        display_name=display_name,
    )
    conversation_id = await get_or_create_conversation_for_user(
        user_id=user_id,
        platform="whatsapp",
        platform_user_id=from_number,
    )

    reply_text = await _run_agent_collect(
        user_id=user_id,
        conversation_id=conversation_id,
        text=text,
    )

    await _send_whatsapp_message(from_number, reply_text, settings)


async def _run_agent_collect(user_id: str, conversation_id: str, text: str) -> str:
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


async def _send_whatsapp_message(to: str, text: str, settings):
    """Send a text reply via the Meta Graph API."""
    # WhatsApp messages are capped at 4096 chars; split if needed
    max_len = 4096
    chunks = [text[i:i + max_len] for i in range(0, len(text), max_len)]

    async with httpx.AsyncClient(timeout=15) as client:
        for chunk in chunks:
            await client.post(
                f"{GRAPH_API_URL}/{settings.whatsapp_phone_number_id}/messages",
                headers={"Authorization": f"Bearer {settings.whatsapp_token}"},
                json={
                    "messaging_product": "whatsapp",
                    "to": to,
                    "type": "text",
                    "text": {"body": chunk},
                },
            )


async def _transcribe_whatsapp_audio(audio: dict, settings) -> str:
    """Download a WhatsApp audio attachment and transcribe via Whisper."""
    from app.services.transcription import transcribe_audio

    media_id = audio.get("id")
    if not media_id:
        return ""

    async with httpx.AsyncClient(timeout=30) as client:
        # Fetch the media URL from Graph API
        meta_resp = await client.get(
            f"{GRAPH_API_URL}/{media_id}",
            headers={"Authorization": f"Bearer {settings.whatsapp_token}"},
        )
        if meta_resp.status_code != 200:
            return ""
        media_url = meta_resp.json().get("url", "")
        if not media_url:
            return ""

        # Download the audio file
        audio_resp = await client.get(
            media_url,
            headers={"Authorization": f"Bearer {settings.whatsapp_token}"},
        )
        if audio_resp.status_code != 200:
            return ""
        audio_bytes = audio_resp.content

    mime_type = audio.get("mime_type", "audio/ogg")
    ext = mime_type.split("/")[-1].split(";")[0]
    return await transcribe_audio(audio_bytes, filename=f"voice.{ext}")


def _verify_signature(body: bytes, signature_header: str, token: str):
    """Validate X-Hub-Signature-256 to ensure the request is from Meta."""
    if not signature_header.startswith("sha256="):
        raise HTTPException(status_code=403, detail="Missing signature")
    expected = hmac.new(token.encode(), body, hashlib.sha256).hexdigest()
    received = signature_header[len("sha256="):]
    if not hmac.compare_digest(expected, received):
        raise HTTPException(status_code=403, detail="Invalid signature")
