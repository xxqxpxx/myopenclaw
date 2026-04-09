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


def _safe_data(result) -> dict | None:
    """Safely extract .data from a maybe_single() result that can be None."""
    if result is None:
        return None
    return result.data


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
    return _safe_data(result)


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
    return _safe_data(result)


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


# ── Sandbox Tracking ───────────────────────────────────────────────────────

async def update_conversation_sandbox(
    conversation_id: str,
    sandbox_id: str | None = None,
    sandbox_state: str | None = None,
) -> None:
    """Update sandbox tracking fields on a conversation."""
    sb = get_supabase()
    fields: dict = {}
    if sandbox_id is not None:
        fields["sandbox_id"] = sandbox_id
    if sandbox_state is not None:
        fields["sandbox_state"] = sandbox_state
    if sandbox_state == "active":
        fields["last_active_at"] = "now()"
    if fields:
        sb.table("conversations").update(fields).eq("id", conversation_id).execute()


async def touch_conversation_activity(conversation_id: str) -> None:
    """Update last_active_at to now (called on each message)."""
    sb = get_supabase()
    sb.table("conversations").update(
        {"last_active_at": "now()"}
    ).eq("id", conversation_id).execute()


async def log_sandbox_event(
    conversation_id: str,
    user_id: str,
    sandbox_id: str,
    state: str,
) -> None:
    """Insert a sandbox lifecycle event into the audit log."""
    sb = get_supabase()
    sb.table("sandbox_sessions").insert({
        "conversation_id": conversation_id,
        "user_id": user_id,
        "sandbox_id": sandbox_id,
        "state": state,
    }).execute()


# ── Files ──────────────────────────────────────────────────────────────────

async def insert_file(
    user_id: str,
    conversation_id: str,
    filename: str,
    size: int,
    storage_path: str,
) -> dict:
    sb = get_supabase()
    result = (
        sb.table("files")
        .insert({
            "user_id": user_id,
            "conversation_id": conversation_id,
            "filename": filename,
            "size": size,
            "storage_path": storage_path,
        })
        .execute()
    )
    return result.data[0]


async def get_files(conversation_id: str) -> list[dict]:
    sb = get_supabase()
    result = (
        sb.table("files")
        .select("*")
        .eq("conversation_id", conversation_id)
        .order("created_at", desc=False)
        .execute()
    )
    return result.data


async def get_file(file_id: str) -> dict | None:
    sb = get_supabase()
    result = (
        sb.table("files")
        .select("*")
        .eq("id", file_id)
        .maybe_single()
        .execute()
    )
    return _safe_data(result)


# ── BYOK API Keys ─────────────────────────────────────────────────────────

async def upsert_api_key(
    user_id: str, provider: str, encrypted_key: str, hint: str
) -> dict:
    sb = get_supabase()
    result = (
        sb.table("api_keys")
        .upsert(
            {
                "user_id": user_id,
                "provider": provider,
                "encrypted_key": encrypted_key,
                "key_hint": hint,
                "is_valid": True,
            },
            on_conflict="user_id,provider",
        )
        .execute()
    )
    return result.data[0]


async def get_api_keys(user_id: str) -> list[dict]:
    sb = get_supabase()
    result = (
        sb.table("api_keys")
        .select("id, provider, key_hint, is_valid, created_at")
        .eq("user_id", user_id)
        .execute()
    )
    return result.data


async def get_api_key_encrypted(user_id: str, provider: str) -> str | None:
    """Fetch the encrypted key blob for a specific provider. Returns None if not set."""
    sb = get_supabase()
    try:
        result = (
            sb.table("api_keys")
            .select("encrypted_key")
            .eq("user_id", user_id)
            .eq("provider", provider)
            .eq("is_valid", True)
            .maybe_single()
            .execute()
        )
        data = _safe_data(result)
        if data:
            return data["encrypted_key"]
    except Exception:
        pass
    return None


async def delete_api_key(user_id: str, provider: str) -> bool:
    sb = get_supabase()
    result = (
        sb.table("api_keys")
        .delete()
        .eq("user_id", user_id)
        .eq("provider", provider)
        .execute()
    )
    return len(result.data) > 0


async def update_user_preferences(user_id: str, **fields) -> dict | None:
    sb = get_supabase()
    result = (
        sb.table("users")
        .update(fields)
        .eq("id", user_id)
        .execute()
    )
    return result.data[0] if result.data else None


# ── Subscriptions ──────────────────────────────────────────────────────────

async def get_subscription(user_id: str) -> dict | None:
    sb = get_supabase()
    result = (
        sb.table("subscriptions")
        .select("*")
        .eq("user_id", user_id)
        .maybe_single()
        .execute()
    )
    return _safe_data(result)


async def upsert_subscription(user_id: str, **fields) -> dict:
    """Upsert a subscription row, updating only the fields that are explicitly provided.

    Kwargs with a value of None are excluded so that a partial call such as
    ``upsert_subscription(user_id, stripe_customer_id=cid)`` never overwrites
    existing tier / status columns with nulls.
    """
    sb = get_supabase()
    update = {k: v for k, v in fields.items() if v is not None}
    update["user_id"] = user_id
    result = (
        sb.table("subscriptions")
        .upsert(update, on_conflict="user_id")
        .execute()
    )
    return result.data[0]


async def get_subscription_by_stripe_customer(customer_id: str) -> dict | None:
    sb = get_supabase()
    result = (
        sb.table("subscriptions")
        .select("*")
        .eq("stripe_customer_id", customer_id)
        .maybe_single()
        .execute()
    )
    return _safe_data(result)


async def add_credits(user_id: str, amount: int) -> int:
    """Add credits to a user's balance. Returns new balance."""
    profile = await get_user_profile(user_id)
    if not profile:
        return 0
    new_balance = profile["credits_balance"] + amount
    sb = get_supabase()
    sb.table("users").update({"credits_balance": new_balance}).eq("id", user_id).execute()
    return new_balance


async def update_user_subscription(user_id: str, tier: str, credits_to_add: int) -> dict:
    """Update a user's subscription tier and add monthly credits.

    Used by billing webhook handlers to activate or downgrade a subscription.
    Returns the updated subscription row.
    """
    result = await upsert_subscription(user_id, tier=tier, status="active")
    await add_credits(user_id, credits_to_add)
    return result


async def log_credit_purchase(
    user_id: str, amount: int, price_usd: float, source: str, payment_id: str | None = None
) -> dict:
    sb = get_supabase()
    result = (
        sb.table("credit_purchases")
        .insert({
            "user_id": user_id,
            "amount": amount,
            "price_usd": price_usd,
            "source": source,
            "payment_id": payment_id,
        })
        .execute()
    )
    return result.data[0]
