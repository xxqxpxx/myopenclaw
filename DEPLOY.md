# myOpenClaw — Production Deploy Checklist

## 1. Backend (Railway)

### Required env vars

| Variable | Required | Notes |
|---|---|---|
| `SUPABASE_URL` | **REQUIRED** | `https://your-project.supabase.co` |
| `SUPABASE_ANON_KEY` | **REQUIRED** | Supabase project anon key |
| `SUPABASE_SERVICE_ROLE_KEY` | **REQUIRED** | Supabase service role key (server-side only) |
| `SUPABASE_JWT_SECRET` | **REQUIRED** | From Supabase → Settings → API → JWT Secret |
| `ANTHROPIC_API_KEY` | **REQUIRED** | `sk-ant-...` |
| `CORS_ORIGINS` | **REQUIRED** | Comma-separated, e.g. `https://app.myopenclaw.com` |
| `APP_ENV` | **REQUIRED** | Set to `production` |
| `APP_DEBUG` | **REQUIRED** | Set to `false` |
| `STRIPE_SECRET_KEY` | **REQUIRED** (billing) | Use `sk_live_...` in production |
| `STRIPE_WEBHOOK_SECRET` | **REQUIRED** (billing) | `whsec_...` from Stripe Dashboard |
| `STRIPE_PRICE_STARTER` | **REQUIRED** (billing) | `price_...` from Stripe Dashboard |
| `STRIPE_PRICE_PRO` | **REQUIRED** (billing) | `price_...` from Stripe Dashboard |
| `STRIPE_PRICE_POWER` | **REQUIRED** (billing) | `price_...` from Stripe Dashboard |
| `STRIPE_PRICE_BYOK` | **REQUIRED** (billing) | `price_...` from Stripe Dashboard |
| `STRIPE_PRICE_TOPUP` | **REQUIRED** (billing) | `price_...` from Stripe Dashboard |
| `BYOK_ENCRYPTION_KEY` | **REQUIRED** (BYOK) | 32-byte hex key for AES-256-GCM user key encryption |
| `E2B_API_KEY` | optional | Required for code execution sandboxes |
| `E2B_SANDBOX_TEMPLATE_ID` | optional | Required for code execution sandboxes |
| `OPENAI_API_KEY` | optional | Required for voice transcription (Whisper) |
| `TELEGRAM_BOT_TOKEN` | optional | Required for Telegram bot integration |
| `WHATSAPP_PHONE_NUMBER_ID` | optional | Required for WhatsApp integration |
| `WHATSAPP_TOKEN` | optional | Required for WhatsApp integration |
| `GOOGLE_CLIENT_ID` | optional | Required for Google OAuth (Gmail/Calendar) |
| `GOOGLE_CLIENT_SECRET` | optional | Required for Google OAuth |
| `GOOGLE_REDIRECT_URI` | optional | `https://your-railway-url/api/v1/auth/google/callback` |
| `SENTRY_DSN` | optional | Enable error tracking |

Railway automatically sets `PORT`. The app reads `APP_ENV`, `APP_DEBUG`, and all vars above via Pydantic settings.

---

## 2. Frontend (Vercel)

| Variable | Required | Value |
|---|---|---|
| `NEXT_PUBLIC_API_URL` | **REQUIRED** | Your Railway backend URL, e.g. `https://myopenclaw.up.railway.app` |
| `NEXT_PUBLIC_SUPABASE_URL` | **REQUIRED** | `https://your-project.supabase.co` |
| `NEXT_PUBLIC_SUPABASE_ANON_KEY` | **REQUIRED** | Supabase anon key (safe to expose) |
| `NEXT_PUBLIC_APP_URL` | **REQUIRED** | Your Vercel URL, e.g. `https://app.myopenclaw.com` |

Set these under Vercel → Project → Settings → Environment Variables. Apply to Production, Preview, and Development as needed.

---

## 3. Supabase Setup

1. Run migrations in order from `backend/migrations/`:
   ```
   001_initial.sql
   002_sandboxes.sql
   003_api_keys.sql
   004_memory.sql
   005_subscriptions.sql
   006_messaging_integrations.sql
   ```
   Run via: Supabase Dashboard → SQL Editor, or `psql $DATABASE_URL -f migrations/00N_*.sql`

2. Verify RLS is enabled on all tables:
   - `users`, `conversations`, `messages`, `usage_logs`, `sandboxes`
   - `user_api_keys`, `memory_entries`, `files`, `subscriptions`
   - Check in: Supabase Dashboard → Table Editor → each table → RLS toggle

3. Create Storage bucket:
   - Name: `files`
   - Public read: enabled
   - Authenticated write: enabled (add policy for `auth.uid() = owner_id` or equivalent)

---

## 4. Stripe Setup

1. In [Stripe Dashboard](https://dashboard.stripe.com/products), create one product per tier and add a recurring price for each:
   - **Starter** → copy price ID → set `STRIPE_PRICE_STARTER`
   - **Pro** → copy price ID → set `STRIPE_PRICE_PRO`
   - **Power** → copy price ID → set `STRIPE_PRICE_POWER`
   - **BYOK** → copy price ID → set `STRIPE_PRICE_BYOK`
   - **Top-up** (one-time) → copy price ID → set `STRIPE_PRICE_TOPUP`

2. Register the webhook in [Stripe Dashboard → Webhooks](https://dashboard.stripe.com/webhooks):
   - Endpoint URL: `POST https://your-railway-url/api/v1/billing/webhooks/stripe`
   - Events to subscribe:
     - `checkout.session.completed`
     - `customer.subscription.updated`
     - `customer.subscription.deleted`
     - `invoice.paid`

3. After saving the webhook, copy the **Signing secret** (`whsec_...`) and set it as `STRIPE_WEBHOOK_SECRET` on Railway.

---

## 5. Google OAuth Setup (optional)

Required only if enabling Gmail / Google Calendar tool access.

1. Go to [Google Cloud Console](https://console.cloud.google.com/) → APIs & Services → Credentials
2. Create an **OAuth 2.0 Client ID** (Web application type)
3. Add authorized redirect URI:
   ```
   https://your-railway-url/api/v1/auth/google/callback
   ```
4. Set on Railway:
   - `GOOGLE_CLIENT_ID`
   - `GOOGLE_CLIENT_SECRET`
   - `GOOGLE_REDIRECT_URI` — must exactly match the URI above

---

## 6. Webhook Endpoints Reference

| Integration | Method | Path |
|---|---|---|
| Stripe | POST | `/api/v1/billing/webhooks/stripe` |
| Telegram | POST | `/webhooks/telegram` |
| WhatsApp (Meta) | POST | `/webhooks/whatsapp` |
| Google OAuth callback | GET | `/api/v1/auth/google/callback` |

---

## 7. Launch Checklist

- [ ] All required env vars set on Railway
- [ ] `APP_ENV=production` and `APP_DEBUG=false` set on Railway
- [ ] All env vars set on Vercel
- [ ] Migrations 001–006 run on Supabase
- [ ] RLS confirmed enabled on all tables
- [ ] Supabase Storage `files` bucket created with correct policies
- [ ] `CORS_ORIGINS` set to Vercel deployment URL (not `*`)
- [ ] Stripe products and prices created; all `STRIPE_PRICE_*` vars set
- [ ] Stripe webhook registered and `STRIPE_WEBHOOK_SECRET` set
- [ ] `GET /health` returns `status: ok` and all expected services green
- [ ] Test signup → chat → credit deduction flow end-to-end
- [ ] Test Stripe checkout → subscription activated in DB
- [ ] (Optional) Google OAuth flow tested with a real account
- [ ] (Optional) Sentry DSN set and a test error appears in dashboard
