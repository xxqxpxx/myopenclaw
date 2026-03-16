# Production Deployment Guide

## Stripe Webhook

The backend handles Stripe events at:

```
POST https://<your-backend-domain>/api/v1/billing/webhooks/stripe
```

### Setup in Stripe Dashboard

1. Go to [Stripe Dashboard → Developers → Webhooks](https://dashboard.stripe.com/webhooks)
2. Click **Add endpoint**
3. **Endpoint URL:** `https://<your-backend-domain>/api/v1/billing/webhooks/stripe`
4. **Events to listen for:**
   - `checkout.session.completed`
   - `customer.subscription.updated`
   - `customer.subscription.deleted`
   - `invoice.paid`
5. Copy the **Signing secret** (starts with `whsec_`)
6. Set `STRIPE_WEBHOOK_SECRET` in your production environment

### Required Stripe Env Vars

| Variable | Description |
|----------|-------------|
| `STRIPE_SECRET_KEY` | Stripe secret key (sk_live_...) |
| `STRIPE_WEBHOOK_SECRET` | Webhook signing secret (whsec_...) |
| `STRIPE_PRICE_STARTER` | Price ID for Starter plan |
| `STRIPE_PRICE_PRO` | Price ID for Pro plan |
| `STRIPE_PRICE_POWER` | Price ID for Power plan |
| `STRIPE_PRICE_BYOK` | Price ID for BYOK plan |
| `STRIPE_PRICE_TOPUP` | Price ID for credit top-ups |

## Checkout Redirect URLs

- **Web:** `createCheckout()` uses `window.location.origin` for success/cancel URLs, so production checkout redirects correctly when users are on your prod domain.
- **Override:** If checkout is opened from a different origin (e.g. mobile webview), set `NEXT_PUBLIC_APP_URL` (e.g. `https://app.myopenclaw.com`) so the web app can pass the correct success URL to the backend.

## E2E Tests

```bash
cd web
# Create a test user in Supabase (Auth → Users)
TEST_USER_EMAIL=... TEST_USER_PASSWORD=... npm run test:e2e
```

Ensure backend, web dev server, and Supabase are running before tests.
