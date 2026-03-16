"""Google OAuth 2.0 flow for Gmail + Calendar access.

Endpoints:
  GET /auth/google          — redirect user to Google consent screen
  GET /auth/google/callback — exchange code for tokens, store encrypted in Supabase

Required env vars:
  GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET, GOOGLE_REDIRECT_URI

The stored token_data JSON is AES-256-GCM encrypted using the same vault key
as BYOK keys (BYOK_ENCRYPTION_KEY).
"""

from __future__ import annotations

import json
import logging
from urllib.parse import urlencode

import httpx
from fastapi import APIRouter, HTTPException, Query
from fastapi.responses import RedirectResponse

from fastapi import Depends
from app.auth.jwt import AuthenticatedUser, get_current_user
from app.config import get_settings
from app.db.supabase import get_supabase
from app.services.vault import encrypt_api_key

logger = logging.getLogger(__name__)

router = APIRouter()

GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth"
GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token"

SCOPES = " ".join([
    "https://www.googleapis.com/auth/gmail.send",
    "https://www.googleapis.com/auth/gmail.readonly",
    "https://www.googleapis.com/auth/calendar",
    "https://www.googleapis.com/auth/calendar.events",
])


@router.get("/auth/google")
async def google_auth_start(current_user: AuthenticatedUser = Depends(get_current_user)):
    """Redirect the authenticated user to Google's OAuth consent screen."""
    settings = get_settings()
    if not settings.google_client_id:
        raise HTTPException(status_code=503, detail="Google OAuth not configured")

    params = {
        "client_id": settings.google_client_id,
        "redirect_uri": settings.google_redirect_uri,
        "response_type": "code",
        "scope": SCOPES,
        "access_type": "offline",
        "prompt": "consent",
        # Embed user_id in state so we can link the token on callback
        "state": current_user.user_id,
    }
    return RedirectResponse(url=f"{GOOGLE_AUTH_URL}?{urlencode(params)}")


@router.get("/auth/google/callback")
async def google_auth_callback(
    code: str = Query(...),
    state: str = Query(...),  # user_id embedded in state
    error: str = Query(default=""),
):
    """Exchange the authorization code for tokens and store them."""
    if error:
        raise HTTPException(status_code=400, detail=f"Google OAuth error: {error}")

    settings = get_settings()
    user_id = state  # We embedded user_id in the state param

    # Exchange code for access + refresh tokens
    async with httpx.AsyncClient(timeout=15) as client:
        resp = await client.post(
            GOOGLE_TOKEN_URL,
            data={
                "code": code,
                "client_id": settings.google_client_id,
                "client_secret": settings.google_client_secret,
                "redirect_uri": settings.google_redirect_uri,
                "grant_type": "authorization_code",
            },
        )

    if resp.status_code != 200:
        logger.error("Google token exchange failed: %s", resp.text)
        raise HTTPException(status_code=502, detail="Failed to exchange Google auth code")

    token_data = resp.json()
    # Include client credentials so we can refresh later
    token_data["client_id"] = settings.google_client_id
    token_data["client_secret"] = settings.google_client_secret

    # Encrypt and upsert into oauth_tokens
    encrypted = encrypt_api_key(json.dumps(token_data))
    db = get_supabase()
    db.table("oauth_tokens").upsert({
        "user_id": user_id,
        "provider": "google",
        "token_data": encrypted,
    }, on_conflict="user_id,provider").execute()

    logger.info("Google OAuth tokens stored for user %s", user_id)
    # Redirect back to the web dashboard settings page
    return RedirectResponse(url="/settings/integrations?connected=google")


@router.delete("/auth/google")
async def google_auth_disconnect(current_user: AuthenticatedUser = Depends(get_current_user)):
    """Remove the stored Google OAuth tokens for the current user."""
    db = get_supabase()
    db.table("oauth_tokens").delete().eq("user_id", current_user.user_id).eq("provider", "google").execute()
    return {"disconnected": "google"}


@router.get("/auth/google/status")
async def google_auth_status(current_user: AuthenticatedUser = Depends(get_current_user)):
    """Check whether the current user has Google connected."""
    db = get_supabase()
    try:
        result = (
            db.table("oauth_tokens")
            .select("id,updated_at")
            .eq("user_id", current_user.user_id)
            .eq("provider", "google")
            .maybe_single()
            .execute()
        )
        data = result.data if result else None
    except Exception:
        data = None
    return {"connected": bool(data), "updated_at": data.get("updated_at") if data else None}
