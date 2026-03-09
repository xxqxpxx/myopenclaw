"""BYOK key resolution — fetch + decrypt user's stored API key."""

from __future__ import annotations

from app.db import supabase as db
from app.services.vault import decrypt_api_key


async def resolve_byok_key(user_id: str, provider: str = "anthropic") -> str | None:
    """Return the decrypted BYOK key for a user, or None to use bundled key."""
    encrypted = await db.get_api_key_encrypted(user_id, provider)
    if not encrypted:
        return None
    return decrypt_api_key(encrypted)
