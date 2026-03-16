# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**myOpenClaw** is a full-stack AI agent platform that wraps OpenClaw (open-source AI agent framework) into consumer products. The system provides Claude-powered agentic chat with code execution sandboxes, cross-platform clients (web + Android/iOS), and a credit-based billing model.

## Commands

### Web Frontend (`/web`)
```bash
npm run dev      # Dev server at http://localhost:3000
npm run build    # Production build
npm run lint     # ESLint
```

### Backend (`/backend`)
```bash
pip install -e ".[dev]"                     # Install with dev dependencies
uvicorn app.main:app --reload --port 8000   # Dev server at http://localhost:8000
pytest                                       # Run all tests
pytest tests/test_vault.py                  # Run a single test file
ruff check                                  # Lint (line-length: 100, target Python 3.12)
ruff check --fix                            # Auto-fix lint issues
```

### Mobile (`/mobile/KMP-App-Template-main`)
```bash
./gradlew composeApp:compileKotlinAndroid   # Compile check (Android target)
./gradlew composeApp:assembleDebug          # Build Android debug APK
```

### Environment Setup

**Backend required env vars** (`backend/.env`):
```
SUPABASE_URL, SUPABASE_ANON_KEY, SUPABASE_SERVICE_ROLE_KEY, SUPABASE_JWT_SECRET
ANTHROPIC_API_KEY
APP_ENV=development
```
Optional: `E2B_API_KEY`, `STRIPE_SECRET_KEY`, `STRIPE_WEBHOOK_SECRET`, `STRIPE_PRICE_*`, `TELEGRAM_BOT_TOKEN`, `WHATSAPP_PHONE_NUMBER_ID`, `WHATSAPP_TOKEN`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `BYOK_ENCRYPTION_KEY` (32-byte hex), `OPENAI_API_KEY` (Whisper), `SENTRY_DSN`, `CORS_ORIGINS` (comma-separated, defaults to `*`)

**Web** (`web/.env.local`): `NEXT_PUBLIC_API_URL`, `NEXT_PUBLIC_SUPABASE_URL`, `NEXT_PUBLIC_SUPABASE_ANON_KEY`

**Mobile** (`ApiConfig.kt`): `BASE_URL` defaults to `http://10.0.2.2:8000` (Android emulator → localhost)

## Architecture

**Request flow:**
```
Clients (Web/Android/iOS) → FastAPI Backend (Railway) → E2B Sandboxes → Anthropic Claude API
                                     ↕
                              Supabase (Auth + DB)
```

The backend is the central authority for auth, credits, model routing, and sandbox lifecycle. Clients are thin — they authenticate with Supabase, then pass the JWT to all backend calls. The backend validates JWTs using `SUPABASE_JWT_SECRET` via `app/auth/jwt.py` (tries raw secret first, then base64-decoded), injecting an `AuthenticatedUser(user_id, email, role)` dependency into every protected route via `Depends(get_current_user)`.

### Backend (`backend/app/`)

- **`api/`** — FastAPI routers, all mounted under `/api/v1` except messaging webhooks. Routers: `conversations`, `users`, `sandboxes`, `files`, `api_keys`, `billing`, `memory`, `google_oauth`. Messaging webhooks mount without prefix: `telegram` (`/webhooks/telegram`), `whatsapp` (`/webhooks/whatsapp`). Stripe webhook: `POST /api/v1/billing/webhooks/stripe`.
- **`services/llm.py`** — Core LLM service. `stream_chat_with_tools()` runs the agentic tool-use loop (up to 10 rounds), emitting SSE `data:` lines: `{"type":"token","content":"…","model":"…"}`, `{"type":"tool_start","tool":"…","input":{…}}`, `{"type":"tool_result","tool":"…","output":"…"}`, `{"type":"file","filename":"…","url":"…","size":N}`, `{"type":"done","total_tokens":N,"credits_used":N}`, `{"type":"error","error":"…"}`. `route_model()` selects Haiku/Sonnet/Opus based on query complexity and token count.
- **`services/tools.py`** — Anthropic tool schemas (`TOOL_DEFINITIONS`) and execution logic. Available tools: `code_execute` (runs in E2B sandbox), `web_search`, `file_read`, `file_write`.
- **`services/sandbox.py`** — E2B Firecracker VM lifecycle (creating → active → idle → paused → destroyed). One sandbox per conversation, idle timeout 10 min, max lifetime 7 days.
- **`services/tiers.py`** — Subscription tier definitions and feature gating logic.
- **`services/bridge.py`** — Agent-to-backend communication bridge.
- **`services/byok.py` + `vault.py`** — AES-256-GCM encryption for user-supplied API keys.
- **`services/transcription.py`** — Audio transcription service.
- **`db/supabase.py`** — All database operations (uses Supabase service role key for server-side access).
- **`models/schemas.py`** — Pydantic request/response models shared across API and services.
- **`config.py`** — Pydantic settings class; all env vars loaded here.

### Web Frontend (`web/src/`)

- **`app/(dashboard)/`** — Protected routes behind Next.js auth middleware (`middleware.ts`).
- **`app/(dashboard)/chat/[id]/page.tsx`** — Main chat view, consumes SSE stream and renders tool events.
- **`lib/api.ts`** — Typed API client. `streamChat()` reads SSE and fires `onEvent` callbacks.
- **`lib/store.ts`** — Zustand global store for conversations, messages, credits, and streaming state. Optimistic updates on send.
- **`lib/supabase.ts`** — Supabase SSR client setup.
- Path alias `@/*` maps to `src/*`.

### Mobile (`mobile/KMP-App-Template-main/`)

Kotlin Multiplatform project. Shared business logic lives in `composeApp/src/commonMain/` (ViewModels, repositories, API client via Ktor, domain models), Android UI in `androidMain/` (Jetpack Compose), iOS UI in `iosMain/` (SwiftUI). DI via Koin. Package: `com.myopenclaw`. The app was migrated from a market-data app ("Signalwhisper") — see `mobile/MOBILE_CONTINUATION_PROMPT.md` for remaining cleanup tasks if the build is broken.

### Database

Supabase PostgreSQL with RLS. Migrations in `backend/migrations/` (run in order 001–006). Key tables: `users`, `conversations`, `messages`, `usage_logs`, `sandboxes`, `user_api_keys`, `memory_entries`, `files`, `subscriptions`, `messaging_users` (006).

## Model Routing & Credits

- **Haiku** (`claude-haiku-4-5-20250315`) → short queries without code
- **Sonnet** (`claude-sonnet-4-20250514`) → code/analysis queries or >200 input tokens
- **Opus** (`claude-opus-4-20250514`) → user-explicit override only
- Credit conversion: 1 credit ≈ $0.001. Free tier starts with 50 credits (`DEFAULT_FREE_CREDITS`). Credits are deducted after stream completes based on actual token usage from the Anthropic response.
- Rate limits: 20 messages/min per user, 100 conversations/user max (configurable via env).

## Phase Status

Phases 1–10 are marked done in the plan. Active untracked work: messaging integrations (Telegram, WhatsApp, Google OAuth) in `backend/app/api/` and `backend/app/db/messaging_users.py`. See `quickclaw-plan.md` for the full roadmap.
