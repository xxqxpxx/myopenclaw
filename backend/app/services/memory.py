"""Memory service — cross-conversation memory management.

Two storage layers:
1. Structured: memory_entries table (for API access)
2. Files: Markdown files in Supabase Storage (for sandbox injection)
"""

from __future__ import annotations

import logging

from app.db import supabase as db

logger = logging.getLogger(__name__)

MEMORY_BUCKET = "user-memory"


async def get_memory_entries(user_id: str, category: str | None = None) -> list[dict]:
    """Fetch all memory entries for a user, optionally filtered by category."""
    sb = db.get_supabase()
    query = sb.table("memory_entries").select("*").eq("user_id", user_id)
    if category:
        query = query.eq("category", category)
    result = query.order("created_at", desc=True).execute()
    return result.data


async def add_memory(user_id: str, content: str, category: str = "general") -> dict:
    """Store a new memory entry."""
    sb = db.get_supabase()
    result = (
        sb.table("memory_entries")
        .insert({"user_id": user_id, "content": content, "category": category})
        .execute()
    )
    return result.data[0]


async def update_memory(user_id: str, memory_id: str, content: str) -> dict | None:
    sb = db.get_supabase()
    result = (
        sb.table("memory_entries")
        .update({"content": content})
        .eq("id", memory_id)
        .eq("user_id", user_id)
        .execute()
    )
    return result.data[0] if result.data else None


async def delete_memory(user_id: str, memory_id: str) -> bool:
    sb = db.get_supabase()
    result = (
        sb.table("memory_entries")
        .delete()
        .eq("id", memory_id)
        .eq("user_id", user_id)
        .execute()
    )
    return len(result.data) > 0


async def clear_all_memory(user_id: str) -> int:
    """Delete all memory entries for a user. Returns count deleted."""
    sb = db.get_supabase()
    result = (
        sb.table("memory_entries")
        .delete()
        .eq("user_id", user_id)
        .execute()
    )
    return len(result.data)


async def build_memory_context(user_id: str) -> str:
    """Build a system prompt snippet from user's memories."""
    entries = await get_memory_entries(user_id)
    if not entries:
        return ""

    by_category: dict[str, list[str]] = {}
    for e in entries:
        by_category.setdefault(e["category"], []).append(e["content"])

    lines = ["\n## User Memory (cross-conversation)"]
    for cat, items in by_category.items():
        lines.append(f"\n### {cat.title()}")
        for item in items:
            lines.append(f"- {item}")

    return "\n".join(lines)


async def search_conversations(user_id: str, query: str, limit: int = 20) -> list[dict]:
    """Full-text search across user's messages. Returns matching conversations."""
    sb = db.get_supabase()
    # Use Postgres full-text search via RPC or raw query
    # The search_vector column is auto-generated via tsvector
    result = (
        sb.table("messages")
        .select("id, conversation_id, role, content, created_at")
        .eq("conversation_id.user_id", user_id)  # RLS handles this
        .text_search("search_vector", query.replace(" ", " & "))
        .limit(limit)
        .execute()
    )
    return result.data
