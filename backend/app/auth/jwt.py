"""JWT authentication middleware for Supabase Auth tokens."""

from __future__ import annotations

from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from jose import JWTError, jwt

from app.config import Settings, get_settings

security = HTTPBearer()


class AuthenticatedUser:
    """Holds the verified user identity extracted from the JWT."""

    def __init__(self, user_id: str, email: str, role: str = "authenticated"):
        self.user_id = user_id
        self.email = email
        self.role = role


async def get_current_user(
    credentials: HTTPAuthorizationCredentials = Depends(security),
    settings: Settings = Depends(get_settings),
) -> AuthenticatedUser:
    """Validate the Supabase JWT and return the authenticated user."""
    token = credentials.credentials
    # Supabase JWT secret may be base64-encoded — try raw first, then decoded
    secret = settings.supabase_jwt_secret
    try:
        payload = jwt.decode(
            token,
            secret,
            algorithms=["HS256"],
            audience="authenticated",
        )
    except JWTError:
        # Try base64-decoded secret (Supabase sometimes provides base64-encoded secret)
        import base64
        try:
            decoded_secret = base64.b64decode(secret + "==")
            payload = jwt.decode(
                token,
                decoded_secret,
                algorithms=["HS256"],
                audience="authenticated",
            )
        except (JWTError, Exception):
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

    return AuthenticatedUser(user_id=user_id, email=email, role=payload.get("role", "authenticated"))
