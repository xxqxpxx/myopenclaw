# Deployment Guide

## Production Checklist

### 1. Stripe Webhook

Register the webhook in [Stripe Dashboard](https://dashboard.stripe.com/webhooks):

- **Endpoint URL:** `https://<your-backend-domain>/api/v1/billing/webhooks/stripe`
- **Events to listen for:**
  - `checkout.session.completed`
  - `customer.subscription.updated`
  - `customer.subscription.deleted`
  - `invoice.paid`
- Copy the **Signing secret** (starts with `whsec_`) and set as `STRIPE_WEBHOOK_SECRET` in production env.

### 2. Success / Cancel URLs

The web app uses `window.location.origin` for Stripe Checkout redirects, so production works automatically when users are on your domain.

If you need an override (e.g. mobile webview, different domain):

- Set `NEXT_PUBLIC_APP_URL` in the web app (e.g. `https://app.myopenclaw.com`)
- Update `createCheckout()` in `web/src/lib/api.ts` to use it when provided

### 3. Environment Variables

**Backend** (see `backend/.env.example`):

| Variable | Required | Notes |
|----------|----------|-------|
| `STRIPE_SECRET_KEY` | For billing | Use `sk_live_...` in production |
| `STRIPE_WEBHOOK_SECRET` | For webhooks | From Stripe Dashboard webhook |
| `STRIPE_PRICE_*` | For checkout | Create prices in Stripe Dashboard |

**Web:**

| Variable | Notes |
|----------|-------|
| `NEXT_PUBLIC_API_URL` | Backend URL (e.g. `https://api.myopenclaw.com`) |
| `NEXT_PUBLIC_SUPABASE_URL` | Supabase project URL |
| `NEXT_PUBLIC_SUPABASE_ANON_KEY` | Supabase anon key |

### 4. CORS

Backend allows `*` origins. Lock down in production:

```python
# app/main.py
allow_origins=["https://app.myopenclaw.com", "https://myopenclaw.com"]
```
