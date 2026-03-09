"""User profile and credits API endpoints."""

from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException

from app.auth.jwt import AuthenticatedUser, get_current_user
from app.db import supabase as db
from app.models.schemas import CreditBalanceResponse, UserProfileResponse

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
