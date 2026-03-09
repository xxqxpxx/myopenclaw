"""
BYOK Key Vault — AES-256-GCM encryption for user API keys.
Keys are encrypted at rest and decrypted only in-memory for the duration of an API call.
"""

import os
import secrets
from cryptography.hazmat.primitives.ciphers.aead import AESGCM

from app.config import get_settings


def _get_aes_key() -> bytes:
    """Get the 32-byte AES key from config, or generate one for dev."""
    hex_key = get_settings().byok_encryption_key
    if hex_key:
        return bytes.fromhex(hex_key)
    # Dev fallback: deterministic key derived from supabase_jwt_secret
    jwt_secret = get_settings().supabase_jwt_secret.encode()
    from hashlib import sha256

    return sha256(jwt_secret).digest()


def encrypt_api_key(plaintext: str) -> str:
    """Encrypt an API key with AES-256-GCM. Returns nonce+ciphertext as hex."""
    key = _get_aes_key()
    nonce = os.urandom(12)  # 96-bit nonce for GCM
    aesgcm = AESGCM(key)
    ct = aesgcm.encrypt(nonce, plaintext.encode(), None)
    return (nonce + ct).hex()


def decrypt_api_key(ciphertext_hex: str) -> str:
    """Decrypt an AES-256-GCM encrypted API key."""
    key = _get_aes_key()
    raw = bytes.fromhex(ciphertext_hex)
    nonce, ct = raw[:12], raw[12:]
    aesgcm = AESGCM(key)
    return aesgcm.decrypt(nonce, ct, None).decode()


def key_hint(api_key: str) -> str:
    """Return last 4 characters for safe display, e.g. '...xK9m'."""
    if len(api_key) < 4:
        return "****"
    return f"...{api_key[-4:]}"


def generate_encryption_key() -> str:
    """Generate a random 32-byte hex key for BYOK_ENCRYPTION_KEY env var."""
    return secrets.token_hex(32)
