"""Audio transcription via OpenAI Whisper API.

Used by Telegram and WhatsApp webhook handlers to convert voice notes to text.
Requires OPENAI_API_KEY in the environment. Falls back gracefully if not configured.
"""

from __future__ import annotations

import io
import logging

from app.config import get_settings

logger = logging.getLogger(__name__)


async def transcribe_audio(audio_bytes: bytes, filename: str = "voice.ogg") -> str:
    """Transcribe audio bytes using OpenAI Whisper.

    Returns the transcribed text, or an empty string on failure.
    Accepts any format Whisper supports: ogg, mp4, webm, mp3, wav, m4a.
    """
    settings = get_settings()
    if not settings.openai_api_key:
        logger.warning("OPENAI_API_KEY not set — voice transcription unavailable")
        return ""

    try:
        import httpx

        async with httpx.AsyncClient(timeout=60) as client:
            resp = await client.post(
                "https://api.openai.com/v1/audio/transcriptions",
                headers={"Authorization": f"Bearer {settings.openai_api_key}"},
                files={"file": (filename, io.BytesIO(audio_bytes), "audio/ogg")},
                data={"model": "whisper-1"},
            )

        if resp.status_code != 200:
            logger.error("Whisper API error %s: %s", resp.status_code, resp.text[:200])
            return ""

        return resp.json().get("text", "").strip()

    except Exception:
        logger.exception("Voice transcription failed")
        return ""
