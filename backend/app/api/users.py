"""User profile and credits API endpoints."""

from __future__ import annotations

import logging

from fastapi import APIRouter, Depends, HTTPException

from app.auth.jwt import AuthenticatedUser, get_current_user
from app.db import supabase as db
from app.models.schemas import CreditBalanceResponse, UserProfileResponse

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/users", tags=["users"])


@router.get("/me", response_model=UserProfileResponse)
async def get_my_profile(user: AuthenticatedUser = Depends(get_current_user)):
    """Get the authenticated user's profile (auto-creates on first call)."""
    profile = await db.ensure_user_profile(user.user_id, user.email)
    return UserProfileResponse(**profile)


@router.get("/me/credits", response_model=CreditBalanceResponse)
async def get_my_credits(user: AuthenticatedUser = Depends(get_current_user)):
    """Get current credit balance."""
    profile = await db.get_user_profile(user.user_id)
    if not profile:
        profile = await db.ensure_user_profile(user.user_id, user.email)
    return CreditBalanceResponse(
        credits_balance=profile["credits_balance"],
        subscription_tier=profile["subscription_tier"],
    )


@router.post("/me/provision")
async def provision_user(user: AuthenticatedUser = Depends(get_current_user)):
    """Provision a new user after signup: ensure profile + create first conversation + warm sandbox.

    Called once by the frontend right after signup. Idempotent — safe to call multiple times.
    """
    # 1. Ensure user profile exists (DB trigger should have created it, but belt-and-suspenders)
    profile = await db.ensure_user_profile(user.user_id, user.email)

    # 2. Check if user already has conversations (idempotent guard)
    existing = await db.get_conversations(user.user_id, limit=1)
    if existing:
        return {
            "status": "already_provisioned",
            "conversation_id": existing[0]["id"],
            "credits_balance": profile["credits_balance"],
        }

    # 3. Create first conversation
    conv = await db.create_conversation(user.user_id, "New Chat")

    # 4. Pre-warm E2B sandbox (non-blocking — don't fail if E2B isn't configured)
    from app.services.bridge import is_sandbox_enabled
    sandbox_status = "skipped"
    if is_sandbox_enabled():
        try:
            from app.services.sandbox import get_sandbox_manager
            manager = get_sandbox_manager()
            await manager.get_or_create(
                user_id=user.user_id,
                conversation_id=conv["id"],
            )
            sandbox_status = "ready"
        except Exception as e:
            logger.warning("Failed to pre-warm sandbox for user %s: %s", user.user_id, e)
            sandbox_status = "failed"

    return {
        "status": "provisioned",
        "conversation_id": conv["id"],
        "credits_balance": profile["credits_balance"],
        "sandbox": sandbox_status,
    }
