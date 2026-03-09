-- Migration 005: Memory & Search
-- Cross-conversation memory and full-text search on messages

-- Memory entries table (alternative to raw files for structured memory)
CREATE TABLE IF NOT EXISTS memory_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    category TEXT NOT NULL DEFAULT 'general', -- personal, preferences, projects, etc.
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

ALTER TABLE memory_entries ENABLE ROW LEVEL SECURITY;
CREATE POLICY "users own their memory" ON memory_entries
    FOR ALL USING (auth.uid() = user_id);

CREATE INDEX IF NOT EXISTS idx_memory_user ON memory_entries(user_id, category);

-- Full-text search on messages
ALTER TABLE messages ADD COLUMN IF NOT EXISTS search_vector tsvector
    GENERATED ALWAYS AS (to_tsvector('english', content)) STORED;
CREATE INDEX IF NOT EXISTS idx_messages_fts ON messages USING GIN(search_vector);
