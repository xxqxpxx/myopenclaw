-- myOpenClaw Database Schema — Supabase PostgreSQL
-- Run this in the Supabase SQL Editor to create all tables

-- ═══════════════════════════════════════════════════════════════════════════
-- Enable UUID generation
-- ═══════════════════════════════════════════════════════════════════════════
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ═══════════════════════════════════════════════════════════════════════════
-- USERS — extends Supabase Auth user with app-specific fields
-- ═══════════════════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS public.users (
    id              UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email           TEXT NOT NULL,
    display_name    TEXT,
    subscription_tier TEXT NOT NULL DEFAULT 'free'
                    CHECK (subscription_tier IN ('free', 'starter', 'pro', 'beast')),
    credits_balance INTEGER NOT NULL DEFAULT 50,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Auto-update updated_at
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER users_updated_at
    BEFORE UPDATE ON public.users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

-- ═══════════════════════════════════════════════════════════════════════════
-- CONVERSATIONS
-- ═══════════════════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS public.conversations (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    title           TEXT NOT NULL DEFAULT 'New Chat',
    sandbox_id      TEXT,
    agent_memory_path TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_conversations_user_id ON public.conversations(user_id);
CREATE INDEX idx_conversations_updated_at ON public.conversations(updated_at DESC);

CREATE TRIGGER conversations_updated_at
    BEFORE UPDATE ON public.conversations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

-- ═══════════════════════════════════════════════════════════════════════════
-- MESSAGES
-- ═══════════════════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS public.messages (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    conversation_id UUID NOT NULL REFERENCES public.conversations(id) ON DELETE CASCADE,
    role            TEXT NOT NULL CHECK (role IN ('user', 'assistant', 'tool', 'system')),
    content         TEXT NOT NULL DEFAULT '',
    model           TEXT,
    tokens_used     INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_messages_conversation_id ON public.messages(conversation_id);
CREATE INDEX idx_messages_created_at ON public.messages(conversation_id, created_at);

-- ═══════════════════════════════════════════════════════════════════════════
-- USAGE LOGS — per-request cost tracking
-- ═══════════════════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS public.usage_logs (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    conversation_id UUID REFERENCES public.conversations(id) ON DELETE SET NULL,
    model           TEXT NOT NULL,
    input_tokens    INTEGER NOT NULL DEFAULT 0,
    output_tokens   INTEGER NOT NULL DEFAULT 0,
    cost_usd        NUMERIC(10, 6) NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_usage_logs_user_id ON public.usage_logs(user_id);
CREATE INDEX idx_usage_logs_created_at ON public.usage_logs(created_at DESC);

-- ═══════════════════════════════════════════════════════════════════════════
-- API KEYS — BYOK encrypted keys (Phase 7)
-- ═══════════════════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS public.api_keys (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    provider        TEXT NOT NULL CHECK (provider IN ('anthropic', 'openai', 'gemini')),
    encrypted_key   TEXT NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_api_keys_user_id ON public.api_keys(user_id);

-- ═══════════════════════════════════════════════════════════════════════════
-- SUBSCRIPTIONS (Phase 8)
-- ═══════════════════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS public.subscriptions (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    plan            TEXT NOT NULL DEFAULT 'free',
    status          TEXT NOT NULL DEFAULT 'active'
                    CHECK (status IN ('active', 'canceled', 'past_due', 'trialing')),
    credits_total   INTEGER NOT NULL DEFAULT 0,
    credits_used    INTEGER NOT NULL DEFAULT 0,
    period_start    TIMESTAMPTZ,
    period_end      TIMESTAMPTZ,
    stripe_subscription_id TEXT,
    revenuecat_id   TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_subscriptions_user_id ON public.subscriptions(user_id);

CREATE TRIGGER subscriptions_updated_at
    BEFORE UPDATE ON public.subscriptions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

-- ═══════════════════════════════════════════════════════════════════════════
-- FILES — agent-created or user-uploaded files
-- ═══════════════════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS public.files (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    conversation_id UUID REFERENCES public.conversations(id) ON DELETE SET NULL,
    filename        TEXT NOT NULL,
    size            INTEGER NOT NULL DEFAULT 0,
    storage_path    TEXT NOT NULL,
    mime_type       TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_files_user_id ON public.files(user_id);
CREATE INDEX idx_files_conversation_id ON public.files(conversation_id);

-- ═══════════════════════════════════════════════════════════════════════════
-- ROW LEVEL SECURITY — every user can only access their own data
-- ═══════════════════════════════════════════════════════════════════════════
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.conversations ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.usage_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.api_keys ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.subscriptions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.files ENABLE ROW LEVEL SECURITY;

-- Users can read/update their own profile
CREATE POLICY users_select ON public.users FOR SELECT USING (auth.uid() = id);
CREATE POLICY users_update ON public.users FOR UPDATE USING (auth.uid() = id);

-- Conversations: full CRUD for own conversations
CREATE POLICY conversations_select ON public.conversations FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY conversations_insert ON public.conversations FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY conversations_update ON public.conversations FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY conversations_delete ON public.conversations FOR DELETE USING (auth.uid() = user_id);

-- Messages: read own, insert into own conversations
CREATE POLICY messages_select ON public.messages FOR SELECT
    USING (conversation_id IN (SELECT id FROM public.conversations WHERE user_id = auth.uid()));
CREATE POLICY messages_insert ON public.messages FOR INSERT
    WITH CHECK (conversation_id IN (SELECT id FROM public.conversations WHERE user_id = auth.uid()));

-- Usage logs: read own
CREATE POLICY usage_logs_select ON public.usage_logs FOR SELECT USING (auth.uid() = user_id);

-- API keys: full CRUD for own keys
CREATE POLICY api_keys_select ON public.api_keys FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY api_keys_insert ON public.api_keys FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY api_keys_update ON public.api_keys FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY api_keys_delete ON public.api_keys FOR DELETE USING (auth.uid() = user_id);

-- Subscriptions: read own
CREATE POLICY subscriptions_select ON public.subscriptions FOR SELECT USING (auth.uid() = user_id);

-- Files: read own, insert into own
CREATE POLICY files_select ON public.files FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY files_insert ON public.files FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY files_delete ON public.files FOR DELETE USING (auth.uid() = user_id);

-- ═══════════════════════════════════════════════════════════════════════════
-- SERVICE ROLE bypass — backend uses service_role key (bypasses RLS)
-- No additional grants needed; service_role key automatically bypasses RLS.
-- ═══════════════════════════════════════════════════════════════════════════
