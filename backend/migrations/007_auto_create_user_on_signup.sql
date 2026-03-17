-- Auto-create a public.users row when a new user signs up via Supabase Auth.
-- This trigger fires on INSERT to auth.users, so the user profile (with 50
-- free credits) exists immediately — no need to wait for the first API call.

CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.users (id, email, subscription_tier, credits_balance)
    VALUES (
        NEW.id,
        COALESCE(NEW.email, ''),
        'free',
        50
    )
    ON CONFLICT (id) DO NOTHING;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Drop existing trigger if any, then create
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;

CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_new_user();
