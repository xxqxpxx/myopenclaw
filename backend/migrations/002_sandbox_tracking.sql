-- Phase 4: Sandbox tracking columns + sandboxes table
-- Run this in Supabase SQL Editor after 001_initial_schema.sql

-- ═══════════════════════════════════════════════════════════════════════════
-- Add sandbox tracking to conversations (sandbox_id already exists from 001)
-- ═══════════════════════════════════════════════════════════════════════════

-- Add last_active_at for idle detection
ALTER TABLE public.conversations
    ADD COLUMN IF NOT EXISTS sandbox_state TEXT DEFAULT NULL
        CHECK (sandbox_state IS NULL OR sandbox_state IN ('active', 'paused', 'destroyed')),
    ADD COLUMN IF NOT EXISTS last_active_at TIMESTAMPTZ;

-- Index for finding idle sandboxes
CREATE INDEX IF NOT EXISTS idx_conversations_sandbox_state
    ON public.conversations(sandbox_state)
    WHERE sandbox_state IS NOT NULL;

-- ═══════════════════════════════════════════════════════════════════════════
-- SANDBOX SESSIONS — audit log of sandbox lifecycle events
-- ═══════════════════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS public.sandbox_sessions (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    conversation_id UUID NOT NULL REFERENCES public.conversations(id) ON DELETE CASCADE,
    user_id         UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    sandbox_id      TEXT NOT NULL,
    state           TEXT NOT NULL CHECK (state IN ('created', 'active', 'paused', 'resumed', 'destroyed')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_sandbox_sessions_conversation
    ON public.sandbox_sessions(conversation_id);

CREATE INDEX IF NOT EXISTS idx_sandbox_sessions_user
    ON public.sandbox_sessions(user_id);

-- ═══════════════════════════════════════════════════════════════════════════
-- RLS policies for sandbox_sessions
-- ═══════════════════════════════════════════════════════════════════════════
ALTER TABLE public.sandbox_sessions ENABLE ROW LEVEL SECURITY;

CREATE POLICY sandbox_sessions_user_read ON public.sandbox_sessions
    FOR SELECT USING (user_id = auth.uid());

CREATE POLICY sandbox_sessions_service_all ON public.sandbox_sessions
    FOR ALL USING (true)
    WITH CHECK (true);
