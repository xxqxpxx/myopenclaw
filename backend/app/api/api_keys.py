"""BYOK API key management endpoints."""

from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException, status
from pydantic import BaseModel, Field

from app.auth.jwt import AuthenticatedUser, get_current_user
from app.db import supabase as db
from app.services.vault import encrypt_api_key, decrypt_api_key, key_hint

router = APIRouter(prefix="/api-keys", tags=["api-keys"])

SUPPORTED_PROVIDERS = {"anthropic", "openai", "google", "deepseek"}


# ── Request / Response models ──────────────────────────────────────────────

class SubmitKeyRequest(BaseModel):
    provider: str = Field(..., description="AI provider name")
    api_key: str = Field(..., min_length=8, description="Raw API key")


class ApiKeyResponse(BaseModel):
    id: str
    provider: str
    key_hint: str
    is_valid: bool
    created_at: str


class PreferencesRequest(BaseModel):
    preferred_model: str | None = None
    preferred_provider: str | None = None


# ── Endpoints ──────────────────────────────────────────────────────────────

@router.get("", response_model=list[ApiKeyResponse])
async def list_keys(user: AuthenticatedUser = Depends(get_current_user)):
    """List all stored API keys (hints only — never returns raw keys)."""
    keys = await db.get_api_keys(user.user_id)
    return keys


@router.post("", response_model=ApiKeyResponse, status_code=status.HTTP_201_CREATED)
async def submit_key(
    body: SubmitKeyRequest,
    user: AuthenticatedUser = Depends(get_current_user),
):
    """Submit (or update) a BYOK API key. The key is validated, encrypted, then stored."""
    if body.provider not in SUPPORTED_PROVIDERS:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Unsupported provider. Must be one of: {', '.join(sorted(SUPPORTED_PROVIDERS))}",
        )

    # Validate the key by making a lightweight API call
    is_valid = await _validate_key(body.provider, body.api_key)
    if not is_valid:
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail="API key validation failed. Please check the key and try again.",
        )

    encrypted = encrypt_api_key(body.api_key)
    hint = key_hint(body.api_key)
    row = await db.upsert_api_key(user.user_id, body.provider, encrypted, hint)
    return row


@router.delete("/{provider}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_key(
    provider: str,
    user: AuthenticatedUser = Depends(get_current_user),
):
    """Delete a stored API key."""
    deleted = await db.delete_api_key(user.user_id, provider)
    if not deleted:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Key not found")


@router.put("/preferences")
async def update_preferences(
    body: PreferencesRequest,
    user: AuthenticatedUser = Depends(get_current_user),
):
    """Update user model/provider preferences."""
    fields = {}
    if body.preferred_model is not None:
        fields["preferred_model"] = body.preferred_model
    if body.preferred_provider is not None:
        fields["preferred_provider"] = body.preferred_provider
    if not fields:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="No fields to update")
    await db.update_user_preferences(user.user_id, **fields)
    return {"status": "ok"}


# ── Key validation helpers ─────────────────────────────────────────────────

async def _validate_key(provider: str, api_key: str) -> bool:
    """Make a minimal API call to confirm the key is working."""
    import httpx

    try:
        async with httpx.AsyncClient(timeout=10) as client:
            if provider == "anthropic":
                # Use a minimal request; accept 200 (success) or 400 (bad request
                # but key is valid — e.g. model not available on their plan).
                # Only reject on 401/403 (auth failure).
                resp = await client.post(
                    "https://api.anthropic.com/v1/messages",
                    headers={
                        "x-api-key": api_key,
                        "anthropic-version": "2023-06-01",
                        "content-type": "application/json",
                    },
                    json={
                        "model": "claude-3-haiku-20240307",
                        "max_tokens": 1,
                        "messages": [{"role": "user", "content": "hi"}],
                    },
                )
                # 401/403 = bad key, anything else means key is valid
                return resp.status_code not in (401, 403)

            elif provider == "openai":
                resp = await client.get(
                    "https://api.openai.com/v1/models",
                    headers={"Authorization": f"Bearer {api_key}"},
                )
                return resp.status_code not in (401, 403)

            elif provider == "google":
                resp = await client.get(
                    "https://generativelanguage.googleapis.com/v1/models",
                    params={"key": api_key},
                )
                return resp.status_code not in (401, 403)

            elif provider == "deepseek":
                resp = await client.get(
                    "https://api.deepseek.com/v1/models",
                    headers={"Authorization": f"Bearer {api_key}"},
                )
                return resp.status_code not in (401, 403)

    except httpx.HTTPError:
        return False

    return False
