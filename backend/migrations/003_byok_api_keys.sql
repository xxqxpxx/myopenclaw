-- Migration 003: BYOK API Keys + User Preferences
-- Stores encrypted user API keys for BYOK mode

CREATE TABLE IF NOT EXISTS api_keys (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    provider TEXT NOT NULL CHECK (provider IN ('anthropic', 'openai', 'google', 'deepseek')),
    encrypted_key TEXT NOT NULL,
    key_hint TEXT NOT NULL,       -- last 4 chars for display, e.g. "...xK9m"
    is_valid BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE(user_id, provider)
);

-- RLS: users can only see/manage their own keys
ALTER TABLE api_keys ENABLE ROW LEVEL SECURITY;
CREATE POLICY "users own their keys" ON api_keys
    FOR ALL USING (auth.uid() = user_id);

-- User preferences (model selection, etc.)
ALTER TABLE profiles ADD COLUMN IF NOT EXISTS preferred_model TEXT DEFAULT 'auto';
ALTER TABLE profiles ADD COLUMN IF NOT EXISTS preferred_provider TEXT DEFAULT 'anthropic';

-- Index for fast lookup during chat
CREATE INDEX IF NOT EXISTS idx_api_keys_user_provider ON api_keys(user_id, provider);
