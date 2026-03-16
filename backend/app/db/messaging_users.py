"""Database helpers for messaging identity mapping.

Maps (platform, platform_user_id) → Supabase user_id and conversation_id.
Creates accounts and conversations on first contact.
"""

from __future__ import annotations

import logging
import uuid

from app.db.supabase import get_supabase

logger = logging.getLogger(__name__)


async def get_or_create_messaging_user(
    platform: str,
    platform_user_id: str,
    display_name: str = "",
) -> str:
    """Return the Supabase user_id for a messaging platform identity.

    On first contact, creates a new user row and links it in messaging_identities.
    """
    client = get_supabase()

    # Check existing identity
    result = (
        client.table("messaging_identities")
        .select("user_id")
        .eq("platform", platform)
        .eq("platform_user_id", platform_user_id)
        .maybe_single()
        .execute()
    )
    if result.data:
        return result.data["user_id"]

    # Create a new user account
    new_user_id = str(uuid.uuid4())
    email = f"{platform}_{platform_user_id}@messaging.myopenclaw.internal"
    client.table("users").insert({
        "id": new_user_id,
        "email": email,
        "display_name": display_name or f"{platform.capitalize()} User",
        "credits_balance": 50,  # default_free_credits
    }).execute()

    # Link identity
    client.table("messaging_identities").insert({
        "user_id": new_user_id,
        "platform": platform,
        "platform_user_id": platform_user_id,
    }).execute()

    logger.info("Created new user %s for %s:%s", new_user_id, platform, platform_user_id)
    return new_user_id


async def get_or_create_conversation_for_user(
    user_id: str,
    platform: str,
    platform_user_id: str,
) -> str:
    """Return the active conversation_id for a messaging user.

    Uses the most recently created conversation, or creates a new one.
    """
    client = get_supabase()

    # Find the latest conversation for this user on this platform
    result = (
        client.table("conversations")
        .select("id")
        .eq("user_id", user_id)
        .eq("metadata->>platform", platform)
        .eq("metadata->>platform_user_id", platform_user_id)
        .order("created_at", desc=True)
        .limit(1)
        .execute()
    )
    if result.data:
        return result.data[0]["id"]

    # Create a new conversation
    new_conv_id = str(uuid.uuid4())
    client.table("conversations").insert({
        "id": new_conv_id,
        "user_id": user_id,
        "title": f"Chat via {platform.capitalize()}",
        "metadata": {
            "platform": platform,
            "platform_user_id": platform_user_id,
        },
    }).execute()

    logger.info("Created conversation %s for user %s (%s)", new_conv_id, user_id, platform)
    return new_conv_id
