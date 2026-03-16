-- Migration 006: messaging identities + OAuth tokens
-- Run after 005_memory_search.sql

-- ── messaging_identities ──────────────────────────────────────────────────
-- Maps a (platform, platform_user_id) pair to a Supabase user.
-- Allows users who contact us via WhatsApp or Telegram to be linked to an
-- account without requiring a separate sign-up flow.

CREATE TABLE IF NOT EXISTS messaging_identities (
    id                 uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id            uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    platform           text NOT NULL,           -- 'telegram' | 'whatsapp'
    platform_user_id   text NOT NULL,           -- Telegram chat_id or WhatsApp phone number
    created_at         timestamptz NOT NULL DEFAULT now(),

    UNIQUE (platform, platform_user_id)
);

-- Index for fast look-ups from webhook handlers
CREATE INDEX IF NOT EXISTS idx_messaging_identities_lookup
    ON messaging_identities (platform, platform_user_id);

-- RLS: users can only see their own identities
ALTER TABLE messaging_identities ENABLE ROW LEVEL SECURITY;

CREATE POLICY "users_own_messaging_identities"
    ON messaging_identities
    FOR ALL
    USING (user_id = auth.uid());

-- ── oauth_tokens ──────────────────────────────────────────────────────────
-- Stores OAuth access/refresh tokens for third-party providers (Google, etc.).
-- token_data is AES-256-GCM encrypted JSON (same pattern as user_api_keys).

CREATE TABLE IF NOT EXISTS oauth_tokens (
    id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider     text NOT NULL,      -- 'google', 'notion', etc.
    token_data   text NOT NULL,      -- encrypted JSON blob
    created_at   timestamptz NOT NULL DEFAULT now(),
    updated_at   timestamptz NOT NULL DEFAULT now(),

    UNIQUE (user_id, provider)
);

CREATE INDEX IF NOT EXISTS idx_oauth_tokens_user_provider
    ON oauth_tokens (user_id, provider);

ALTER TABLE oauth_tokens ENABLE ROW LEVEL SECURITY;

CREATE POLICY "users_own_oauth_tokens"
    ON oauth_tokens
    FOR ALL
    USING (user_id = auth.uid());
