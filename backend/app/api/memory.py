"""Memory management and search API endpoints."""

from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException, status
from pydantic import BaseModel, Field

from app.auth.jwt import AuthenticatedUser, get_current_user
from app.services.memory import (
    get_memory_entries,
    add_memory,
    update_memory,
    delete_memory,
    clear_all_memory,
    search_conversations,
)

router = APIRouter(prefix="/memory", tags=["memory"])


# ── Models ─────────────────────────────────────────────────────────────────

class MemoryEntryRequest(BaseModel):
    content: str = Field(..., min_length=1, max_length=2000)
    category: str = "general"


class MemoryEntryResponse(BaseModel):
    id: str
    category: str
    content: str
    created_at: str


class SearchRequest(BaseModel):
    query: str = Field(..., min_length=1)
    limit: int = 20


# ── Endpoints ──────────────────────────────────────────────────────────────

@router.get("", response_model=list[MemoryEntryResponse])
async def list_memories(
    category: str | None = None,
    user: AuthenticatedUser = Depends(get_current_user),
):
    """List all memory entries, optionally filtered by category."""
    return await get_memory_entries(user.user_id, category)


@router.post("", response_model=MemoryEntryResponse, status_code=status.HTTP_201_CREATED)
async def create_memory(
    body: MemoryEntryRequest,
    user: AuthenticatedUser = Depends(get_current_user),
):
    """Add a new memory entry."""
    return await add_memory(user.user_id, body.content, body.category)


@router.put("/{memory_id}", response_model=MemoryEntryResponse)
async def edit_memory(
    memory_id: str,
    body: MemoryEntryRequest,
    user: AuthenticatedUser = Depends(get_current_user),
):
    """Update an existing memory entry."""
    result = await update_memory(user.user_id, memory_id, body.content)
    if not result:
        raise HTTPException(status_code=404, detail="Memory entry not found")
    return result


@router.delete("/{memory_id}", status_code=status.HTTP_204_NO_CONTENT)
async def remove_memory(
    memory_id: str,
    user: AuthenticatedUser = Depends(get_current_user),
):
    """Delete a memory entry."""
    deleted = await delete_memory(user.user_id, memory_id)
    if not deleted:
        raise HTTPException(status_code=404, detail="Memory entry not found")


@router.delete("", status_code=status.HTTP_204_NO_CONTENT)
async def clear_memory(user: AuthenticatedUser = Depends(get_current_user)):
    """Delete all memory entries."""
    await clear_all_memory(user.user_id)


@router.post("/search")
async def search(
    body: SearchRequest,
    user: AuthenticatedUser = Depends(get_current_user),
):
    """Full-text search across all conversations."""
    results = await search_conversations(user.user_id, body.query, body.limit)
    return {"results": results}
