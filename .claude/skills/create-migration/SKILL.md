---
name: create-migration
description: Scaffold a new Supabase SQL migration file with RLS boilerplate. Usage: /create-migration <table_name> [description]
disable-model-invocation: true
---

# Create Migration

Create a new numbered SQL migration file in `backend/migrations/`.

## Steps

1. Find the highest existing migration number in `backend/migrations/` (files named `NNN_*.sql`)
2. Increment by 1, zero-padded to 3 digits
3. Create the file at `backend/migrations/<NNN>_<table_name>.sql` with this template:

```sql
-- Migration: <NNN>_<table_name>
-- Description: <description or "Add <table_name> table">
-- Date: <today's date>

-- Create table
CREATE TABLE IF NOT EXISTS public.<table_name> (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES public.users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW() NOT NULL
);

-- Enable RLS
ALTER TABLE public.<table_name> ENABLE ROW LEVEL SECURITY;

-- RLS Policies
CREATE POLICY "<table_name>_select_own" ON public.<table_name>
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "<table_name>_insert_own" ON public.<table_name>
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "<table_name>_update_own" ON public.<table_name>
    FOR UPDATE USING (auth.uid() = user_id);

CREATE POLICY "<table_name>_delete_own" ON public.<table_name>
    FOR DELETE USING (auth.uid() = user_id);

-- Updated_at trigger
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN NEW.updated_at = NOW(); RETURN NEW; END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_<table_name>_updated_at
    BEFORE UPDATE ON public.<table_name>
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

4. Show the created file path and remind the user to add any custom columns before running.
