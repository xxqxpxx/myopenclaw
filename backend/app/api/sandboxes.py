"""Sandbox lifecycle API endpoints — manage E2B sandboxes for conversations."""

from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel

from app.auth.jwt import AuthenticatedUser, get_current_user
from app.db import supabase as db
from app.services.sandbox import get_sandbox_manager, SandboxState

router = APIRouter(prefix="/sandboxes", tags=["sandboxes"])


# ── Response schemas ───────────────────────────────────────────────────────

class SandboxStatusResponse(BaseModel):
    conversation_id: str
    sandbox_id: str | None = None
    state: str
    active_count: int


class SandboxActionResponse(BaseModel):
    conversation_id: str
    action: str
    success: bool


# ── Endpoints ──────────────────────────────────────────────────────────────

@router.get("/{conversation_id}", response_model=SandboxStatusResponse)
async def get_sandbox_status(
    conversation_id: str,
    user: AuthenticatedUser = Depends(get_current_user),
):
    """Get the sandbox status for a conversation."""
    conv = await db.get_conversation(conversation_id, user.user_id)
    if not conv:
        raise HTTPException(status_code=404, detail="Conversation not found")

    manager = get_sandbox_manager()
    info = manager.get_sandbox_info(conversation_id)

    return SandboxStatusResponse(
        conversation_id=conversation_id,
        sandbox_id=info.sandbox_id if info else None,
        state=info.state.value if info else "none",
        active_count=manager.get_active_count(),
    )


@router.post("/{conversation_id}/pause", response_model=SandboxActionResponse)
async def pause_sandbox(
    conversation_id: str,
    user: AuthenticatedUser = Depends(get_current_user),
):
    """Pause (snapshot) the sandbox for a conversation to save costs."""
    conv = await db.get_conversation(conversation_id, user.user_id)
    if not conv:
        raise HTTPException(status_code=404, detail="Conversation not found")

    manager = get_sandbox_manager()
    success = await manager.pause_sandbox(conversation_id)

    if success:
        info = manager.get_sandbox_info(conversation_id)
        await db.update_conversation_sandbox(
            conversation_id, sandbox_state="paused"
        )
        if info:
            await db.log_sandbox_event(
                conversation_id, user.user_id, info.sandbox_id, "paused"
            )

    return SandboxActionResponse(
        conversation_id=conversation_id,
        action="pause",
        success=success,
    )


@router.post("/{conversation_id}/destroy", response_model=SandboxActionResponse)
async def destroy_sandbox(
    conversation_id: str,
    user: AuthenticatedUser = Depends(get_current_user),
):
    """Destroy the sandbox for a conversation (frees all resources)."""
    conv = await db.get_conversation(conversation_id, user.user_id)
    if not conv:
        raise HTTPException(status_code=404, detail="Conversation not found")

    manager = get_sandbox_manager()
    info = manager.get_sandbox_info(conversation_id)
    sandbox_id = info.sandbox_id if info else None

    success = await manager.destroy_sandbox(conversation_id)

    if success and sandbox_id:
        await db.update_conversation_sandbox(
            conversation_id, sandbox_state="destroyed"
        )
        await db.log_sandbox_event(
            conversation_id, user.user_id, sandbox_id, "destroyed"
        )

    return SandboxActionResponse(
        conversation_id=conversation_id,
        action="destroy",
        success=success,
    )
