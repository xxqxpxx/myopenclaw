"""JWT authentication middleware for Supabase Auth tokens."""

from __future__ import annotations

import base64
import json
import logging
from functools import lru_cache

import httpx
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from jose import JWTError, jwt
from jose.utils import base64url_decode

from app.config import Settings, get_settings

logger = logging.getLogger(__name__)
security = HTTPBearer()


class AuthenticatedUser:
    """Holds the verified user identity extracted from the JWT."""

    def __init__(self, user_id: str, email: str, role: str = "authenticated"):
        self.user_id = user_id
        self.email = email
        self.role = role


# Cache JWKS for 1 hour (Supabase rotates keys infrequently)
_jwks_cache: dict | None = None


async def _fetch_jwks(supabase_url: str) -> dict:
    """Fetch JWKS from Supabase's well-known endpoint."""
    global _jwks_cache
    if _jwks_cache is not None:
        return _jwks_cache
    jwks_url = f"{supabase_url}/auth/v1/.well-known/jwks.json"
    async with httpx.AsyncClient() as client:
        resp = await client.get(jwks_url, timeout=10)
        resp.raise_for_status()
        _jwks_cache = resp.json()
        return _jwks_cache


def _get_signing_key_from_jwks(jwks: dict, kid: str) -> dict:
    """Find the matching key in JWKS by kid."""
    for key in jwks.get("keys", []):
        if key.get("kid") == kid:
            return key
    raise ValueError(f"Key {kid} not found in JWKS")


async def _decode_es256(token: str, supabase_url: str) -> dict:
    """Decode a JWT signed with ES256 using Supabase JWKS."""
    from cryptography.hazmat.primitives.asymmetric.ec import (
        EllipticCurvePublicNumbers,
        SECP256R1,
    )
    from cryptography.hazmat.primitives.serialization import Encoding, PublicFormat

    # Extract kid from token header
    header_b64 = token.split(".")[0]
    # Add padding
    header_b64 += "=" * (4 - len(header_b64) % 4)
    header = json.loads(base64url_decode(header_b64.encode()))
    kid = header.get("kid")

    jwks = await _fetch_jwks(supabase_url)
    jwk = _get_signing_key_from_jwks(jwks, kid)

    # Build EC public key from JWK x, y coordinates
    x_bytes = base64url_decode(jwk["x"].encode())
    y_bytes = base64url_decode(jwk["y"].encode())
    x_int = int.from_bytes(x_bytes, "big")
    y_int = int.from_bytes(y_bytes, "big")

    public_numbers = EllipticCurvePublicNumbers(x_int, y_int, SECP256R1())
    public_key = public_numbers.public_key()
    pem = public_key.public_bytes(Encoding.PEM, PublicFormat.SubjectPublicKeyInfo)

    return jwt.decode(
        token,
        pem.decode(),
        algorithms=["ES256"],
        audience="authenticated",
    )


async def get_current_user(
    credentials: HTTPAuthorizationCredentials = Depends(security),
    settings: Settings = Depends(get_settings),
) -> AuthenticatedUser:
    """Validate the Supabase JWT and return the authenticated user."""
    token = credentials.credentials

    # Detect algorithm from token header
    try:
        header_b64 = token.split(".")[0]
        header_b64 += "=" * (4 - len(header_b64) % 4)
        header = json.loads(base64url_decode(header_b64.encode()))
        alg = header.get("alg", "HS256")
    except Exception:
        alg = "HS256"

    payload = None

    if alg == "ES256":
        # Newer Supabase projects use ES256 with JWKS
        try:
            payload = await _decode_es256(token, settings.supabase_url)
        except Exception as e:
            logger.debug("ES256 decode failed: %s", e)
            # Clear JWKS cache and retry once
            global _jwks_cache
            _jwks_cache = None
            try:
                payload = await _decode_es256(token, settings.supabase_url)
            except Exception:
                pass

    if payload is None and alg == "HS256":
        # Legacy Supabase projects use HS256 with JWT secret
        secret = settings.supabase_jwt_secret
        try:
            payload = jwt.decode(
                token, secret, algorithms=["HS256"], audience="authenticated"
            )
        except JWTError:
            try:
                decoded_secret = base64.b64decode(secret + "==")
                payload = jwt.decode(
                    token, decoded_secret, algorithms=["HS256"], audience="authenticated"
                )
            except (JWTError, Exception):
                pass

    if payload is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or expired token",
            headers={"WWW-Authenticate": "Bearer"},
        )

    user_id = payload.get("sub")
    email = payload.get("email", "")

    if not user_id:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Token missing user identity",
        )

    return AuthenticatedUser(
        user_id=user_id, email=email, role=payload.get("role", "authenticated")
    )
