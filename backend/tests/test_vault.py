"""Tests for vault encryption service — BYOK key encrypt/decrypt."""

import pytest
from app.services.vault import encrypt_api_key, decrypt_api_key, key_hint


class TestVault:
    def test_encrypt_decrypt_roundtrip(self):
        key = "sk-ant-api03-my-test-key-1234567890"
        encrypted = encrypt_api_key(key)
        assert encrypted != key
        decrypted = decrypt_api_key(encrypted)
        assert decrypted == key

    def test_different_encryptions_differ(self):
        """Each encryption uses a random nonce, so outputs should differ."""
        key = "sk-ant-some-key"
        e1 = encrypt_api_key(key)
        e2 = encrypt_api_key(key)
        assert e1 != e2
        # But both decrypt to same value
        assert decrypt_api_key(e1) == key
        assert decrypt_api_key(e2) == key

    def test_key_hint(self):
        assert key_hint("sk-ant-api03-xK9m") == "...xK9m"
        assert key_hint("ab") == "****"
        assert key_hint("") == "****"

    def test_corrupt_ciphertext_fails(self):
        key = "test-key"
        encrypted = encrypt_api_key(key)
        # Corrupt one character
        corrupted = encrypted[:-2] + "ff"
        with pytest.raises(Exception):
            decrypt_api_key(corrupted)
