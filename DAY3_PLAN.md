# Day 3 Plan — E2E, Memory UI, Deploy Config

Resume this plan tomorrow. Tasks below with file references and implementation notes.

---

## 1. End-to-End Test

**Goal:** Full flow: sign up → chat → tool call → file download → buy credits → payment success

### Current State
- No E2E tests exist (no `*.spec.*`, no `e2e/` folder)
- Web uses Next.js 16, no Playwright/Cypress installed

### Implementation

1. **Add Playwright** (recommended for Next.js):
   ```bash
   cd web && npm init playwright@latest
   ```
   - Choose Chromium (or all browsers)
   - Create `web/e2e/` folder

2. **Test flow** (`web/e2e/full-flow.spec.ts`):
   - **Sign up:** Use Supabase auth (email/password or magic link). May need test user or `@supabase/supabase-js` test helpers.
   - **Chat:** Create conversation, send message, wait for SSE stream to complete
   - **Tool call:** Send prompt that triggers `code_execute` (e.g. "Run `print('hello')` in Python") — verify `tool_start` / `tool_result` in UI
   - **File download:** Send prompt that triggers `file_create` (e.g. "Create a file called test.txt with 'hello'") — verify `file` event with Download button, click and verify download
   - **Buy credits:** Click plan in Settings → redirect to Stripe Checkout
   - **Payment success:** Use Stripe test mode + test card `4242 4242 4242 4242` — complete checkout → redirect to `?billing=success` → verify credits updated

3. **Environment:**
   - Backend + web + Supabase must be running
   - Use `.env.test` or env vars for test Supabase project
   - Stripe test keys for checkout flow

4. **Key files to reference:**
   - `web/src/app/(dashboard)/chat/[id]/page.tsx` — chat UI, file download link
   - `web/src/app/(dashboard)/settings/page.tsx` — billing section, `?billing=success` handling
   - `web/src/lib/store.ts` — SSE handling, `file` event → `url`
   - `web/src/lib/api.ts` — `createCheckout()`, success_url from `window.location.origin`

---

## 2. Memory Page (View / Delete Memories)

**Goal:** Settings screen shows memory entries with view and delete.

### Current State
- **Backend:** Full memory API at `/api/v1/memory`:
  - `GET /memory` — list entries (optional `?category=`)
  - `POST /memory` — create
  - `PUT /memory/{id}` — edit
  - `DELETE /memory/{id}` — delete one
  - `DELETE /memory` — clear all
  - `POST /memory/search` — search conversations (different from memory entries)
- **Web:** No memory API calls or UI. Settings page has no memory section.

### Implementation

1. **Add memory API functions** in `web/src/lib/api.ts`:
   ```ts
   export interface MemoryEntry {
     id: string;
     category: string;
     content: string;
     created_at: string;
   }
   export async function listMemories(category?: string): Promise<MemoryEntry[]>
   export async function deleteMemory(id: string): Promise<void>
   export async function clearAllMemories(): Promise<void>
   ```

2. **Add Memory section** to `web/src/app/(dashboard)/settings/page.tsx`:
   - "Memory" section between Integrations and Billing (or after Billing)
   - "You have X facts stored" summary
   - List of memory entries (card per entry: content, category, created_at)
   - Delete button per entry
   - "Clear all memory" button with confirmation
   - Load on mount via `listMemories()`

3. **Optional:** Dedicated `/settings/memory` page if list gets long — for now inline in Settings is fine per quickclaw-plan.

4. **Key files:**
   - `backend/app/api/memory.py` — endpoints, `MemoryEntryResponse`
   - `backend/app/services/memory.py` — `get_memory_entries`, `delete_memory`, `clear_all_memory`

---

## 3. Final Deploy Config (Production success_url, Stripe Webhook)

**Goal:** Production checkout redirects correctly; Stripe webhook registered for production.

### Current State
- `createCheckout()` uses `window.location.origin` for success/cancel URLs → works when web is served from prod domain
- Backend `CheckoutRequest` defaults: `http://localhost:3000/settings?billing=success`
- Stripe webhook handler: `POST /api/v1/billing/webhooks/stripe`
- `main.py` comment: "configure this in the Stripe dashboard"

### Implementation

1. **Production success_url:**
   - Web already passes `origin` from `window.location.origin` — correct when user is on prod
   - If checkout is opened from a different origin (e.g. mobile webview), consider `NEXT_PUBLIC_APP_URL` env var for production
   - For web-only: current behavior is fine. Add `NEXT_PUBLIC_APP_URL` only if you need override (e.g. `https://app.myopenclaw.com`)

2. **Stripe Dashboard — Webhook registration:**
   - Go to Stripe Dashboard → Developers → Webhooks → Add endpoint
   - **Endpoint URL:** `https://<your-backend-domain>/api/v1/billing/webhooks/stripe`
   - **Events to listen for:**
     - `checkout.session.completed`
     - `customer.subscription.updated`
     - `customer.subscription.deleted`
     - `invoice.paid`
   - Copy **Signing secret** → set as `STRIPE_WEBHOOK_SECRET` in production env

3. **Document in README or deploy docs:**
   - Webhook URL format
   - Required Stripe env vars: `STRIPE_SECRET_KEY`, `STRIPE_WEBHOOK_SECRET`, `STRIPE_PRICE_*`

4. **Optional:** Add `STRIPE_WEBHOOK_SECRET` check in startup — log warning if empty in production.

---

## Checklist

- [x] Playwright installed, `web/e2e/` created
- [x] E2E: sign up flow
- [x] E2E: chat + tool call
- [x] E2E: file download (in full-flow)
- [x] E2E: buy credits + Stripe redirect
- [x] Memory API in `api.ts`
- [x] Memory section in Settings page (list, delete, clear all)
- [ ] Stripe webhook registered in production Stripe dashboard (manual)
- [x] Production env vars documented (docs/DEPLOY.md)
