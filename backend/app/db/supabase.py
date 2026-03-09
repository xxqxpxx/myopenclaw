"""Supabase client initialization and database operations."""

from __future__ import annotations

from supabase import create_client, Client

from app.config import get_settings


_client: Client | None = None


def get_supabase() -> Client:
    """Return a cached Supabase client (service-role for backend operations)."""
    global _client
    if _client is None:
        settings = get_settings()
        _client = create_client(settings.supabase_url, settings.supabase_service_role_key)
    return _client


# ── Conversations ──────────────────────────────────────────────────────────

async def create_conversation(user_id: str, title: str = "New Chat") -> dict:
    sb = get_supabase()
    result = (
        sb.table("conversations")
        .insert({"user_id": user_id, "title": title})
        .execute()
    )
    return result.data[0]


async def get_conversations(user_id: str, limit: int = 50, offset: int = 0) -> list[dict]:
    sb = get_supabase()
    result = (
        sb.table("conversations")
        .select("*")
        .eq("user_id", user_id)
        .order("updated_at", desc=True)
        .range(offset, offset + limit - 1)
        .execute()
    )
    return result.data


async def get_conversation(conversation_id: str, user_id: str) -> dict | None:
    sb = get_supabase()
    result = (
        sb.table("conversations")
        .select("*")
        .eq("id", conversation_id)
        .eq("user_id", user_id)
        .maybe_single()
        .execute()
    )
    return result.data


async def update_conversation(conversation_id: str, user_id: str, **fields) -> dict | None:
    sb = get_supabase()
    result = (
        sb.table("conversations")
        .update(fields)
        .eq("id", conversation_id)
        .eq("user_id", user_id)
        .execute()
    )
    return result.data[0] if result.data else None


async def delete_conversation(conversation_id: str, user_id: str) -> bool:
    sb = get_supabase()
    result = (
        sb.table("conversations")
        .delete()
        .eq("id", conversation_id)
        .eq("user_id", user_id)
        .execute()
    )
    return len(result.data) > 0


# ── Messages ───────────────────────────────────────────────────────────────

async def insert_message(
    conversation_id: str,
    role: str,
    content: str,
    model: str | None = None,
    tokens_used: int = 0,
) -> dict:
    sb = get_supabase()
    row = {
        "conversation_id": conversation_id,
        "role": role,
        "content": content,
        "tokens_used": tokens_used,
    }
    if model:
        row["model"] = model
    result = sb.table("messages").insert(row).execute()
    # Touch the conversation's updated_at
    sb.table("conversations").update(
        {"updated_at": "now()"}
    ).eq("id", conversation_id).execute()
    return result.data[0]


async def get_messages(
    conversation_id: str, limit: int = 100, offset: int = 0
) -> list[dict]:
    sb = get_supabase()
    result = (
        sb.table("messages")
        .select("*")
        .eq("conversation_id", conversation_id)
        .order("created_at", desc=False)
        .range(offset, offset + limit - 1)
        .execute()
    )
    return result.data


# ── Usage Tracking ─────────────────────────────────────────────────────────

async def log_usage(
    user_id: str,
    model: str,
    input_tokens: int,
    output_tokens: int,
    cost_usd: float,
    conversation_id: str | None = None,
) -> dict:
    sb = get_supabase()
    result = (
        sb.table("usage_logs")
        .insert({
            "user_id": user_id,
            "conversation_id": conversation_id,
            "model": model,
            "input_tokens": input_tokens,
            "output_tokens": output_tokens,
            "cost_usd": cost_usd,
        })
        .execute()
    )
    return result.data[0]


# ── User Profile ───────────────────────────────────────────────────────────

async def get_user_profile(user_id: str) -> dict | None:
    sb = get_supabase()
    result = (
        sb.table("users")
        .select("*")
        .eq("id", user_id)
        .maybe_single()
        .execute()
    )
    return result.data


async def ensure_user_profile(user_id: str, email: str) -> dict:
    """Get or create user profile row (called after first auth)."""
    profile = await get_user_profile(user_id)
    if profile:
        return profile
    sb = get_supabase()
    settings = get_settings()
    result = (
        sb.table("users")
        .insert({
            "id": user_id,
            "email": email,
            "subscription_tier": "free",
            "credits_balance": settings.default_free_credits,
        })
        .execute()
    )
    return result.data[0]


async def deduct_credits(user_id: str, amount: int) -> int:
    """Deduct credits and return new balance. Raises if insufficient."""
    profile = await get_user_profile(user_id)
    if not profile:
        raise ValueError("User not found")
    new_balance = profile["credits_balance"] - amount
    if new_balance < 0:
        raise ValueError("Insufficient credits")
    sb = get_supabase()
    sb.table("users").update(
        {"credits_balance": new_balance}
    ).eq("id", user_id).execute()
    return new_balance
