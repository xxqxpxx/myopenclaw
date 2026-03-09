-- Migration 004: Subscriptions & Billing
-- Subscription tiers, Stripe/RevenueCat integration

CREATE TABLE IF NOT EXISTS subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE UNIQUE,
    tier TEXT NOT NULL DEFAULT 'free' CHECK (tier IN ('free', 'starter', 'pro', 'power', 'byok')),
    status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'past_due', 'cancelled', 'expired')),
    -- Stripe fields (web purchases)
    stripe_customer_id TEXT,
    stripe_subscription_id TEXT,
    -- RevenueCat fields (mobile purchases)
    revenuecat_app_user_id TEXT,
    revenuecat_entitlement TEXT,
    -- Billing cycle
    current_period_start TIMESTAMPTZ,
    current_period_end TIMESTAMPTZ,
    monthly_credits INT NOT NULL DEFAULT 100,
    credits_reset_at TIMESTAMPTZ DEFAULT now(),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- Credit top-up purchases (one-time)
CREATE TABLE IF NOT EXISTS credit_purchases (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    amount INT NOT NULL,
    price_usd NUMERIC(8,2) NOT NULL,
    source TEXT NOT NULL CHECK (source IN ('stripe', 'revenuecat', 'admin')),
    payment_id TEXT, -- Stripe PaymentIntent ID or RevenueCat transaction ID
    created_at TIMESTAMPTZ DEFAULT now()
);

-- RLS
ALTER TABLE subscriptions ENABLE ROW LEVEL SECURITY;
CREATE POLICY "users own their subscription" ON subscriptions
    FOR ALL USING (auth.uid() = user_id);

ALTER TABLE credit_purchases ENABLE ROW LEVEL SECURITY;
CREATE POLICY "users own their purchases" ON credit_purchases
    FOR ALL USING (auth.uid() = user_id);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_subscriptions_stripe ON subscriptions(stripe_customer_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_revenuecat ON subscriptions(revenuecat_app_user_id);
