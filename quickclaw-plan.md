# ⚡ QuickClaw Clone — Full Architecture & Build Plan

> **Kotlin Multiplatform · Android · iOS · Web Dashboard · BYOK + Bundled Keys**

| Attribute            | Detail                                                         |
| -------------------- | -------------------------------------------------------------- |
| **Platforms**        | Android, iOS (Kotlin Multiplatform), Web Dashboard (Next.js)   |
| **Core Engine**      | OpenClaw (open-source, MIT license, 277k+ GitHub stars)        |
| **Primary AI Model** | Claude (Anthropic) — bundled keys OR BYOK                      |
| **Build Duration**   | ~10 weeks to MVP, 14 weeks to production-ready                 |
| **Key Services**     | Supabase · E2B · Anthropic API · RevenueCat · Railway / Fly.io |

---

## Table of Contents

1. [System Architecture Overview](#1-system-architecture-overview)
2. [Phase-by-Phase Build Plan](#2-phase-by-phase-build-plan)
   - [Phase 1 — Backend Foundation](#phase-1--backend-foundation-weeks-12)
   - [Phase 2 — Core LLM + SSE Streaming](#phase-2--core-llm--sse-streaming-week-23)
   - [Phase 3 — KMP Mobile Clients](#phase-3--kotlin-multiplatform-mobile-clients-weeks-35)
   - [Phase 4 — OpenClaw Agent + E2B Sandboxes](#phase-4--openclaw-agent-integration--e2b-sandboxes-weeks-56)
   - [Phase 5 — Tool Execution Layer](#phase-5--tool-execution-layer-weeks-67)
   - [Phase 6 — Web Dashboard](#phase-6--web-dashboard-nextjs-weeks-78)
   - [Phase 7 — BYOK + Key Vault](#phase-7--byok--key-vault-weeks-89)
   - [Phase 8 — Billing & Credits](#phase-8--billing-credits--subscriptions-weeks-910)
   - [Phase 9 — Memory & Persistence](#phase-9--memory-persistence--file-storage-weeks-1011)
   - [Phase 10 — Polish, Testing & Launch](#phase-10--polish-testing--launch-weeks-1214)
3. [Complete Tech Stack](#3-complete-tech-stack--services)
4. [Unit Economics](#4-unit-economics--cost-model)
5. [14-Week Timeline](#5-14-week-timeline)
6. [Risks & Mitigations](#6-risks--mitigations)

---

# 1. System Architecture Overview

The entire system follows a **thin-client architecture**. All the heavy lifting — LLM calls, agent execution, file management, web browsing — happens on the server. Every platform (Android, iOS, Web) is simply a UI that communicates with the same backend over HTTPS and Server-Sent Events (SSE).

## 1.1 High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    📱  CLIENTS  (Thin UI Layer)              │
│  Android App (KMP)  │  iOS App (KMP)  │  Web Dashboard      │
│  Jetpack Compose    │  SwiftUI        │  Next.js + Tailwind  │
└──────────────────────────┬──────────────────────────────────┘
                           │  HTTPS REST + SSE Streaming
┌──────────────────────────▼──────────────────────────────────┐
│               🔀  API GATEWAY  (FastAPI / Railway)           │
│  Auth (Supabase)  │  Credits/Billing  │  Usage Metering     │
│  Key Vault        │  Model Router     │  Sandbox Manager     │
└──────────────────────────┬──────────────────────────────────┘
                           │  WebSocket / REST
┌──────────────────────────▼──────────────────────────────────┐
│            🤖  AGENT LAYER  (OpenClaw per user)              │
│  OpenClaw Gateway  │  ReAct Brain  │  Tool Dispatcher        │
│  Memory (Markdown) │  Skills       │  Multi-Agent Spawner    │
└──────────────────────────┬──────────────────────────────────┘
          Firecracker microVM (E2B sandbox — 1 per user session)
┌──────────────────────────▼──────────────────────────────────┐
│                 🔧  TOOL EXECUTION LAYER                     │
│  Code Execution   │  Browser (CDP)  │  File System           │
│  (Python/Node/Bash)│  Web Browsing  │  HTTP/API Calls        │
└──────────────────────────┬──────────────────────────────────┘
                           │  LLM API Calls
┌──────────────────────────▼──────────────────────────────────┐
│         🧠  AI MODELS  (Bundled Keys OR BYOK)                │
│  Claude Haiku (fast/cheap)  │  Claude Sonnet (smart)         │
│  Claude Opus (heavy tasks)  │  User's own key (BYOK)         │
│  OpenAI GPT-4o  │  Gemini 2.5  │  DeepSeek  │  Ollama       │
└──────────────────────────┬──────────────────────────────────┘
                           │  Read / Write
┌──────────────────────────▼──────────────────────────────────┐
│         🗄️  DATA LAYER  (Supabase PostgreSQL + Storage)      │
│  Users  │  Conversations  │  Messages  │  Credits            │
│  Agent Memory  │  Uploaded Files  │  Usage Logs              │
└─────────────────────────────────────────────────────────────┘
```

## 1.2 Component Connection Map

Every connection in the system — what protocol it uses and what data flows through it:

| From                     | →   | To                         | Protocol & Data                                           |
| ------------------------ | --- | -------------------------- | --------------------------------------------------------- |
| Android / iOS App (KMP)  | →   | API Gateway                | HTTPS POST — user message + auth JWT token                |
| API Gateway              | →   | Android / iOS App          | SSE stream — agent tokens, tool events, file URLs         |
| Web Dashboard (Next.js)  | →   | API Gateway                | HTTPS REST + EventSource — same endpoints as mobile       |
| API Gateway              | →   | Supabase Auth              | JWT verify — validate every request                       |
| API Gateway              | →   | Supabase DB                | PostgreSQL — read credits, write messages, update usage   |
| API Gateway              | →   | Key Vault (Supabase Vault) | Read encrypted API key — bundled OR BYOK                  |
| API Gateway              | →   | E2B Sandbox Manager        | REST — create / resume / pause sandbox per user session   |
| E2B Sandbox              | →   | OpenClaw Gateway           | WebSocket port 18789 — message routing into agent process |
| OpenClaw Brain (ReAct)   | →   | Anthropic API              | HTTPS — LLM call with tool schemas, streaming response    |
| OpenClaw Tool Dispatcher | →   | Code Runner (E2B)          | In-process — Python/Node/Bash execution in microVM        |
| OpenClaw Tool Dispatcher | →   | Browser (Chromium CDP)     | WebSocket — headless browser automation in microVM        |
| OpenClaw Tool Dispatcher | →   | File System (microVM)      | POSIX I/O — read/write files in isolated environment      |
| OpenClaw Memory          | →   | Supabase Storage           | HTTPS — persist/load Markdown memory files per user       |
| RevenueCat SDK           | →   | App Stores / Stripe        | Native — subscription purchase, webhook → API Gateway     |
| API Gateway              | →   | RevenueCat API             | HTTPS — verify subscription, grant/deduct credits         |

## 1.3 Key Design Decisions

### Why Kotlin Multiplatform (KMP)?

KMP lets you share all business logic — API calls, data models, auth, credit management, agent message parsing, streaming — between Android and iOS in a single Kotlin codebase. Only the UI layer is platform-specific (Jetpack Compose for Android, SwiftUI for iOS). This means **60–70% code reuse** with native performance on both platforms, no React Native bridge overhead, and full access to platform APIs.

### Why SSE over WebSockets for streaming?

Server-Sent Events (SSE) are unidirectional (server → client), which is exactly what AI token streaming needs. SSE works through all CDNs, proxies, and load balancers without configuration. It uses standard HTTP/2 multiplexing. WebSockets add bidirectional complexity that is unnecessary here — the user sends one message (a single HTTP POST), then listens for the stream. Both iOS `URLSession` and Android `OkHttp` have native SSE support.

### Why OpenClaw as the agent engine?

OpenClaw (MIT license) already solves the hardest problems: the ReAct agent loop, 13,700+ community skills, multi-agent spawning, browser automation, code execution, memory management, and 20+ channel integrations. Building this from scratch would take 6+ months. Wrapping OpenClaw takes 2 weeks.

### BYOK vs. Bundled Keys

Both modes are supported simultaneously.

- **No API Key mode** — users pay credits, backend uses server-side Anthropic keys with intelligent model routing (80% Haiku, 18% Sonnet, 2% Opus) to stay profitable.
- **BYOK mode** — users paste their own API key (Anthropic, OpenAI, Gemini, etc.) into the app. It is encrypted with AES-256-GCM and stored in Supabase Vault, never logged, and used directly for their sessions. BYOK users pay a flat subscription for the infrastructure only.

---

# 2. Phase-by-Phase Build Plan

| Phase | Name                     | Duration   | Key Deliverable                                  |
| ----- | ------------------------ | ---------- | ------------------------------------------------ |
| P1    | Backend Foundation       | Week 1–2   | Auth, DB schema, API skeleton on Railway         |
| P2    | Core LLM + Streaming     | Week 2–3   | Claude API calls streaming over SSE              |
| P3    | KMP Mobile Clients       | Week 3–5   | Android + iOS apps chatting with backend         |
| P4    | OpenClaw Agent + E2B     | Week 5–6   | Full OpenClaw agent in E2B sandbox per user      |
| P5    | Tool Execution Layer     | Week 6–7   | Code run, web browse, file I/O inside agent      |
| P6    | Web Dashboard            | Week 7–8   | Next.js web app with full feature parity         |
| P7    | BYOK + Key Vault         | Week 8–9   | Encrypted BYOK flow, multi-provider support      |
| P8    | Billing & Credits        | Week 9–10  | RevenueCat + Stripe, credit gates, subscriptions |
| P9    | Memory & Persistence     | Week 10–11 | Cross-session memory, file storage, search       |
| P10   | Polish, Testing & Launch | Week 12–14 | App Store + Play Store + web production launch   |

---

## Phase 1 — Backend Foundation (Weeks 1–2) ✅ DONE

> Everything else depends on this phase. We build the skeleton that all clients talk to, the database schema that stores all state, and the authentication system that secures every request.

### What Gets Built

- Supabase project: PostgreSQL database with all tables, RLS policies, and auth configured
- FastAPI application deployed on Railway with health checks and environment config
- JWT middleware that validates every request against Supabase Auth
- Core database tables (see schema below)
- Basic CRUD endpoints: `POST /conversations`, `POST /messages`, `GET /conversations/{id}/messages`
- Supabase Vault setup for encrypted API key storage (used in Phase 7)
- Railway deployment pipeline with environment variables and secrets management
- Logging with Sentry (error tracking) and basic observability

### Database Schema

| Table           | Key Columns                                                                       | Purpose                                      |
| --------------- | --------------------------------------------------------------------------------- | -------------------------------------------- |
| `users`         | id, email, created_at, subscription_tier, credits_balance                         | Core user record, linked to Supabase Auth    |
| `conversations` | id, user_id, title, created_at, agent_memory_path, sandbox_id                     | Each chat session, links to user and sandbox |
| `messages`      | id, conversation_id, role (user/assistant/tool), content, tokens_used, created_at | Every message in every conversation          |
| `usage_logs`    | id, user_id, model, input_tokens, output_tokens, cost_usd, created_at             | Per-request cost tracking for billing        |
| `api_keys`      | id, user_id, provider (anthropic/openai/gemini), encrypted_key, is_active         | BYOK keys encrypted via Supabase Vault       |
| `subscriptions` | id, user_id, plan, status, credits_total, credits_used, period_end                | Subscription + credit balance management     |
| `files`         | id, user_id, conversation_id, filename, size, storage_path, created_at            | Files created or uploaded by the agent       |

### Connections Established

```
API Gateway (Railway)  →  Supabase Auth       [JWT validation on every request]
API Gateway (Railway)  →  Supabase DB         [Read/write all tables via supabase-py]
Supabase Auth          →  Email / OAuth        [User registration and login]
```

### Go / No-Go Criteria

- [ ] All endpoints return correct responses with valid auth tokens
- [ ] Invalid tokens receive 401 responses
- [ ] Database migrations run cleanly with all RLS policies active
- [ ] Railway deploy pipeline runs in under 2 minutes

---

## Phase 2 — Core LLM + SSE Streaming (Week 2–3) ✅ DONE

> Wire the Anthropic Claude API to the backend and stream tokens back to clients. This establishes the fundamental loop: user sends text → backend calls Claude → tokens stream back in real time. No agent yet — just a smart chatbot.

### What Gets Built

- Anthropic Python SDK integrated in FastAPI with streaming enabled
- `POST /chat/stream` endpoint that accepts a message and streams back SSE
- Token counting middleware: every request logs input + output tokens to `usage_logs`
- Model router: classify query complexity → route to Haiku (simple) or Sonnet (complex)
- Conversation context assembly: fetch last N messages from DB, inject as history
- Error handling: API rate limits, context window overflow, network timeouts

### SSE Event Format (Contract Used by All Clients)

All three clients (Android, iOS, Web) consume the same event stream. The format is:

```
# Token being streamed
data: {"type":"token","content":"Hello ","model":"claude-haiku-4-5"}

# Agent is starting a tool call
data: {"type":"tool_start","tool":"code_execute","input":{"code":"print('hello')"}}

# Tool finished executing
data: {"type":"tool_result","tool":"code_execute","output":"hello"}

# Agent created a file
data: {"type":"file","filename":"report.pdf","url":"https://storage.supabase.co/...","size":45000}

# Stream complete
data: {"type":"done","total_tokens":1204,"credits_used":3}
```

### Model Routing Logic

| Condition                         | Model                | Cost per M tokens      | Use Case         |
| --------------------------------- | -------------------- | ---------------------- | ---------------- |
| Query < 100 tokens, no code/files | Claude Haiku 4.5     | $1.00 in / $5.00 out   | ~80% of queries  |
| Query requires analysis, writing  | Claude Sonnet 4.6    | $3.00 in / $15.00 out  | ~18% of queries  |
| Complex multi-step, BYOK Opus     | Claude Opus 4.6      | $15.00 in / $75.00 out | ~2%, power users |
| BYOK user with own key            | User-specified model | User pays directly     | BYOK tier only   |

### Connections Established

```
API Gateway  →  Anthropic API     [HTTPS with streaming, Bearer auth with bundled key]
API Gateway  →  usage_logs table  [Write token counts + model + cost per request]
API Gateway  →  messages table    [Write assistant response after stream completes]
```

### Go / No-Go Criteria

- [ ] Tokens stream to client within 500ms of sending a message
- [ ] Token counts are accurately logged to `usage_logs`
- [ ] Model router correctly sends simple queries to Haiku and complex to Sonnet
- [ ] Prompt caching enabled and verified working (check `cache_read_input_tokens` in API response)

---

## Phase 3 — Kotlin Multiplatform Mobile Clients (Weeks 3–5) 🟡 IN PROGRESS

> Build the Android and iOS native apps using Kotlin Multiplatform. The shared module handles all business logic. Platform-specific UI uses Jetpack Compose (Android) and SwiftUI (iOS).

### KMP Project Structure

```
/
├── shared/
│   ├── commonMain/         ← All business logic (both platforms)
│   │   ├── AgentRepository.kt
│   │   ├── AuthRepository.kt
│   │   ├── SSEParser.kt
│   │   ├── CreditManager.kt
│   │   ├── FileManager.kt
│   │   └── SettingsStore.kt
│   ├── androidMain/        ← Android-specific implementations
│   │   └── (OkHttp engine, DataStore, etc.)
│   └── iosMain/            ← iOS-specific implementations
│       └── (Darwin HTTP engine, NSUserDefaults, etc.)
├── androidApp/
│   └── ui/                 ← Jetpack Compose screens
│       ├── ChatScreen.kt
│       ├── HomeScreen.kt
│       ├── SettingsScreen.kt
│       └── PaywallScreen.kt
└── iosApp/
    └── ui/                 ← SwiftUI screens (calls KMP viewmodels via Swift interop)
        ├── ChatView.swift
        ├── HomeView.swift
        ├── SettingsView.swift
        └── PaywallView.swift
```

### Shared Module — Key Components

| Component         | Responsibilities                                                                   |
| ----------------- | ---------------------------------------------------------------------------------- |
| `AgentRepository` | `sendMessage()`, `streamResponse()`, `loadConversations()`, `createConversation()` |
| `AuthRepository`  | `signIn()`, `signUp()`, `signOut()`, `refreshToken()`, `observeAuthState()`        |
| `SSEParser`       | Parses SSE event format from Phase 2, emits typed Kotlin sealed classes            |
| `CreditManager`   | `fetchBalance()`, `deductCredits()`, `observeBalance()` — synced from Supabase     |
| `FileManager`     | `uploadFile()`, `downloadFile()`, `listFiles()` — delegates to Supabase Storage    |
| `SettingsStore`   | Stores auth tokens, BYOK key (encrypted at rest), theme, notification prefs        |

### UI Screens (Both Platforms)

| Screen                   | Description                                                                          | Key Interactions                                    |
| ------------------------ | ------------------------------------------------------------------------------------ | --------------------------------------------------- |
| Home / Conversation List | All past conversations with titles and last message preview                          | Tap to open, swipe to delete, FAB for new chat      |
| Chat Screen              | Main agent interaction: message bubbles, streaming tokens, tool cards, file previews | Send message, view tool execution, tap file to open |
| Tool Execution Card      | Expandable card showing tool name, input, progress, output                           | Inline in chat stream, expand/collapse              |
| File Viewer              | Preview generated files (PDF, images, code, text) with download                      | Tap file bubble in chat                             |
| Settings Screen          | Profile, subscription, credits balance, BYOK key input, model selection              | Accessed from nav bar                               |
| Paywall / Upgrade        | Subscription tiers, feature comparison, purchase button                              | RevenueCat purchase flow                            |
| BYOK Screen              | Provider picker, API key input, validation, key management                           | Accessed from Settings                              |
| Onboarding               | 3-screen intro: capabilities, quick setup, first prompt suggestion                   | First launch only                                   |

### Streaming UI Pattern

As SSE tokens arrive, the chat screen appends characters to the last message bubble in real time using `StateFlow` (Android) / `Combine` (iOS). Tool execution events show an animated card with spinner. File events show a downloadable card. The typing cursor disappears on the `done` event.

### Connections Established

```
Android App  →  API Gateway /chat/stream      [OkHttp SSE — POST message, stream tokens]
iOS App      →  API Gateway /chat/stream      [URLSession bytes stream — same endpoint]
Android/iOS  →  Supabase Auth (via SDK)       [Sign in/up, JWT token management]
Android/iOS  →  API Gateway /conversations    [HTTPS REST — load/create/delete]
```

### Go / No-Go Criteria

- [ ] Android app streams tokens smoothly with no dropped frames
- [ ] iOS app streams tokens smoothly in SwiftUI
- [ ] Conversations persist across app restarts
- [ ] Auth token refreshes automatically when expired

---

## Phase 4 — OpenClaw Agent Integration + E2B Sandboxes (Weeks 5–6) ✅ DONE

> Upgrade from a simple chatbot to a real autonomous agent. Each user session gets a dedicated OpenClaw process running inside a Firecracker microVM (via E2B). The agent can now plan multi-step tasks and execute tools.

### What Gets Built

- E2B Python SDK integrated in FastAPI: `create_sandbox()`, `resume_sandbox()`, `pause_sandbox()`
- Sandbox lifecycle manager: maps `user_id + conversation_id → active E2B sandbox ID`
- Custom E2B sandbox image with OpenClaw pre-installed and pre-configured
- OpenClaw config injection: on sandbox creation, inject API key via environment variables
- **Message bridge**: API Gateway receives user message → forwards to OpenClaw WebSocket (port 18789) → OpenClaw calls Claude → streams back through Gateway → SSE to client
- Sandbox state in DB: `conversations` table stores `sandbox_id`, `last_active_at` for resume logic
- Auto-pause: sandboxes idle for 10 minutes are snapshotted and paused (zero cost while idle)
- Auto-resume: on new message, resume from snapshot in < 200ms

### Sandbox Lifecycle State Machine

```
User sends first message
        │
        ▼
  ┌─────────────┐     10 min idle      ┌─────────────┐
  │    ACTIVE   │──────────────────────▶│    IDLE     │
  │  ($0.10/hr) │                      │  ($0.00/hr) │
  └──────┬──────┘                      └──────┬──────┘
         │ New message                        │ New message
         │ arrives                            │ arrives
         │                            ┌───────▼──────┐
         │                            │   RESUMING   │
         │                            │  (< 200ms)   │
         │                            └───────┬──────┘
         │                                    │
         └────────────────────────────────────┘
                                              │ User deletes / 7-day TTL
                                              ▼
                                        ┌──────────┐
                                        │ DESTROYED │
                                        │  ($0.00) │
                                        └──────────┘
```

| State     | Trigger                                | Action                                   | Cost                        |
| --------- | -------------------------------------- | ---------------------------------------- | --------------------------- |
| New       | First message in conversation          | E2B create + OpenClaw start (~3–5 sec)   | $0.000028/sec while running |
| Active    | User sending messages                  | Messages forwarded to OpenClaw WebSocket | ~$0.10/hour                 |
| Idle      | No message for 10 minutes              | Snapshot microVM state, pause sandbox    | $0                          |
| Resuming  | New message, sandbox was paused        | Resume from snapshot (< 200ms)           | $0.000028/sec               |
| Destroyed | User deletes conversation or 7-day TTL | E2B destroy, delete snapshot             | $0                          |

### OpenClaw Configuration Per Sandbox

When a sandbox is created for a user, these environment variables and config are injected by the API Gateway:

```bash
ANTHROPIC_API_KEY=<bundled_key_or_user_byok_key>
OPENCLAW_MODEL=<haiku|sonnet|opus>
OPENCLAW_USER_ID=<user_id>
OPENCLAW_MEMORY_PATH=/workspace/memory/<user_id>/
OPENCLAW_SKILLS=<comma-separated list based on subscription tier>
```

### Connections Established

```
API Gateway  →  E2B Sandbox Manager API           [HTTPS REST — create/pause/resume/destroy]
API Gateway  →  OpenClaw Gateway (WS port 18789)  [WebSocket inside sandbox — forward messages]
OpenClaw Brain  →  Anthropic API                  [HTTPS from inside sandbox — agent LLM calls]
OpenClaw Memory  →  Supabase Storage              [HTTPS — sync memory Markdown files]
```

### Go / No-Go Criteria

- [ ] Sandbox created and OpenClaw ready within 5 seconds of first message
- [ ] Sandbox resumes from snapshot in under 500ms
- [ ] Messages forwarded correctly through Gateway → WebSocket → OpenClaw → SSE back to client
- [ ] User A cannot access User B's sandbox (isolation test)

---

## Phase 5 — Tool Execution Layer (Weeks 6–7) ✅ DONE

> Activate the agent's real-world capabilities: code execution, web browsing, file creation, and external API calls. All run inside the E2B microVM, completely isolated from other users.

### Tools to Implement

| Tool Name      | Capability                                  | Implementation                                | UX Event Sent to Client                          |
| -------------- | ------------------------------------------- | --------------------------------------------- | ------------------------------------------------ |
| `code_execute` | Run Python, Node.js, or Bash code           | OpenClaw built-in, runs in microVM            | `tool_start` → code text → output → `tool_done`  |
| `web_browse`   | Visit URLs, extract content, click elements | Headless Chromium via CDP inside microVM      | `tool_start` → URL → summary → `tool_done`       |
| `web_search`   | Search the web and return results           | Brave Search API or SerpAPI                   | `tool_start` → query → result list → `tool_done` |
| `file_create`  | Create files (PDF, DOCX, CSV, code)         | Code execution writes to `/workspace/output/` | `file` event with download URL                   |
| `file_read`    | Read user-uploaded files                    | User uploads → Supabase → mounted in microVM  | `tool_start` → filename → content summary        |
| `http_request` | Call external APIs                          | Direct HTTP from inside microVM               | `tool_start` → URL → response status + body      |
| `memory_read`  | Read agent's persistent memory              | Read `/workspace/memory/{user_id}/*.md`       | Transparent — no UX event                        |
| `memory_write` | Save a fact to memory                       | Write to memory Markdown file                 | Brief "Saved to memory" indicator                |

### File Flow: Agent Creates a File → User Downloads It

```
1. Agent writes file to /workspace/output/report.pdf inside microVM
2. OpenClaw emits file_created event to API Gateway via WebSocket
3. API Gateway reads file from E2B sandbox filesystem via E2B SDK
4. API Gateway uploads file to Supabase Storage (user-files/{user_id}/)
5. API Gateway generates signed URL (valid 24 hours), inserts row into files table
6. SSE event sent to client: {"type":"file","filename":"report.pdf","url":"...","size":45000}
7. Client renders a downloadable file card in the chat UI
```

### User File Upload Flow

```
1. User selects file in app (image, PDF, CSV, etc.)
2. App uploads directly to Supabase Storage via presigned upload URL
3. App sends message with file reference: {"file_id": "abc123"}
4. API Gateway mounts file read-only in sandbox at /workspace/uploads/
5. Agent reads the file as part of the task
```

### Connections Established

```
OpenClaw Tool Dispatcher  →  Python/Node/Bash runtime  [Subprocess in microVM]
OpenClaw Tool Dispatcher  →  Chromium (CDP WebSocket)   [In-microVM browser automation]
API Gateway               →  Supabase Storage            [Upload agent-created files]
API Gateway               →  Brave Search / SerpAPI      [Web search results for agent]
```

### Go / No-Go Criteria

- [ ] Agent can write and execute a Python script and return the output in chat
- [ ] Agent can visit a URL and extract text content
- [ ] Agent can create a PDF and the client receives a working download link
- [ ] File isolation: agent cannot access files belonging to another user

---

## Phase 6 — Web Dashboard (Next.js) (Weeks 7–8) ✅ DONE

> Build the web application with full feature parity to the mobile apps. The web dashboard shares the same backend API and database — it is just another client.

### Tech Stack

| Layer            | Technology                | Reason                                            |
| ---------------- | ------------------------- | ------------------------------------------------- |
| Framework        | Next.js 15 (App Router)   | SSR, file-based routing, API routes, excellent DX |
| Styling          | Tailwind CSS + shadcn/ui  | Fast, consistent, accessible component library    |
| State Management | Zustand + React Query     | Simple global state + server cache management     |
| Auth             | Supabase Auth JS SDK      | Same auth system as mobile                        |
| Streaming        | EventSource API (SSE)     | Native browser SSE — identical events to mobile   |
| File Preview     | react-pdf + Monaco Editor | PDF preview in-browser, code syntax highlighting  |
| Payments         | Stripe.js                 | Web users pay via Stripe — no App Store 30% cut   |
| Deployment       | Vercel                    | Next.js native, global CDN, preview deployments   |

### Routes / Pages

| Route               | Purpose                                                          |
| ------------------- | ---------------------------------------------------------------- |
| `/`                 | Landing page with marketing and signup CTA                       |
| `/login`, `/signup` | Supabase Auth flows                                              |
| `/dashboard`        | Home: conversation list, New Chat button, usage stats            |
| `/chat/[id]`        | Full-width chat with split panel (chat left, file preview right) |
| `/settings`         | Profile, BYOK key management, model selection, subscription      |
| `/billing`          | Stripe portal, credit top-ups, invoice history                   |
| `/files`            | All agent-created files across conversations, downloadable       |

### Web Chat UI Layout

```
┌─────────────────────────────────────────────────────────┐
│  Sidebar         │  Chat Panel         │  File Preview   │
│                  │                     │                 │
│  [Conversation   │  [Message bubbles]  │  [Auto-updates  │
│   list]          │  [Tool cards]       │   when agent    │
│                  │  [Streaming tokens] │   creates a     │
│  [+ New Chat]    │                     │   file]         │
│                  │  [  Input box  ]    │                 │
└─────────────────────────────────────────────────────────┘
```

The 3-panel layout is a significant UX advantage over mobile for power users working with documents and code. The web also supports keyboard shortcuts: `Cmd+Enter` to send, `Cmd+K` for command palette.

### Connections Established

```
Web Dashboard  →  API Gateway /chat/stream    [EventSource (SSE) — same endpoints as mobile]
Web Dashboard  →  Supabase Auth JS            [Browser SDK — JWT stored in httpOnly cookie]
Web Dashboard  →  Stripe Billing Portal       [HTTPS — subscription management]
Web Dashboard  →  Supabase Storage            [Signed URL — direct file download from CDN]
```

### Go / No-Go Criteria

- [ ] Web chat streams tokens identically to mobile
- [ ] File preview panel updates in real time when agent creates a file
- [ ] Supabase auth session shared between mobile and web (same user can use both)
- [ ] Stripe subscription purchase works end-to-end

---

## Phase 7 — BYOK + Key Vault (Weeks 8–9) ✅ DONE

> Allow power users to supply their own AI provider API keys. Keys must be encrypted at rest and never logged.

### BYOK Security Architecture

| Step            | Action                                                | Security Measure                                               |
| --------------- | ----------------------------------------------------- | -------------------------------------------------------------- |
| 1. Input        | User pastes API key in Settings screen                | Key never stored in app state beyond the input field           |
| 2. Transmission | `POST /api-keys` with key in body over HTTPS          | TLS 1.3 in transit, key in request body (not URL)              |
| 3. Encryption   | Backend encrypts key with AES-256-GCM                 | Per-user encryption key derived from Supabase Vault master key |
| 4. Storage      | Encrypted ciphertext stored in `api_keys` table       | Raw key is **never** stored anywhere — only ciphertext         |
| 5. Usage        | On each request, decrypt in-memory, inject to sandbox | Key exists in plaintext only for microseconds, never logged    |
| 6. Validation   | Test key with a minimal API call before saving        | User sees "Key valid ✓" or specific error message              |
| 7. Deletion     | `DELETE /api-keys/{id}` zeros ciphertext in DB        | Key unrecoverable after deletion                               |

### Supported Providers (BYOK)

- **Anthropic** — Claude Haiku, Sonnet, Opus (all models)
- **OpenAI** — GPT-4o, GPT-4o-mini, o1, o3
- **Google** — Gemini 2.5 Pro, Gemini 2.5 Flash
- **DeepSeek** — DeepSeek-V3, DeepSeek-R1
- **Ollama** — any locally running model (user must expose their Ollama URL)

### Model Selection UI

BYOK users see a model picker in Settings. The selected model is stored in the user's profile in Supabase, read by the API Gateway on each request, and passed to the OpenClaw config when the sandbox is created or resumed. Bundled-key users see Haiku/Sonnet/Opus selection gated by their subscription tier.

### Connections Established

```
Settings Screen (all clients)  →  POST /api-keys        [HTTPS — submit BYOK key]
API Gateway                    →  Supabase Vault         [Retrieve key → decrypt → inject to sandbox]
OpenClaw Sandbox               →  User's AI Provider     [HTTPS from inside microVM with user's key]
```

### Go / No-Go Criteria

- [ ] BYOK key is validated before storage (test API call succeeds)
- [ ] Key is not present in any logs, error messages, or API responses
- [ ] BYOK user's sandbox uses their key (verify via usage dashboard on their AI provider account)
- [ ] Key deletion removes all traces from the database

---

## Phase 8 — Billing, Credits & Subscriptions (Weeks 9–10) ✅ DONE

> Implement the full monetization system. Mobile apps use RevenueCat. Web users use Stripe. Credits are a universal currency abstracting per-token costs from users.

### Subscription Tiers

| Tier    | Monthly Price  | Credits / Month | Features                                               | Model Access   |
| ------- | -------------- | --------------- | ------------------------------------------------------ | -------------- |
| Free    | $0             | 100 credits     | Chat only, no file creation, no code execution         | Haiku only     |
| Starter | $9.99          | 2,000 credits   | All tools, file creation, web browsing, 5 GB storage   | Haiku + Sonnet |
| Pro     | $24.99         | 8,000 credits   | All Starter + priority queuing, longer context, 20 GB  | Haiku + Sonnet |
| Power   | $74.99         | 30,000 credits  | All Pro + Opus access, 100 GB storage, REST API access | All models     |
| BYOK    | $14.99         | Unlimited       | Bring your own key — no credit limits                  | User's choice  |
| Top-Up  | $4.99 one-time | +1,000 credits  | Credit top-up, no expiry                               | Tier-limited   |

### Credit Cost Per Operation

| Operation                          | Credits Deducted | Approx. Real Cost |
| ---------------------------------- | ---------------- | ----------------- |
| 1 Haiku response (avg 500 tokens)  | 1 credit         | $0.003            |
| 1 Sonnet response (avg 500 tokens) | 3 credits        | $0.009            |
| 1 Opus response (avg 500 tokens)   | 15 credits       | $0.045            |
| Code execution (per 30-second run) | 5 credits        | $0.050            |
| Web browse (per page visited)      | 2 credits        | $0.005            |
| Web search (per query)             | 1 credit         | $0.003            |
| File creation (PDF, DOCX, etc.)    | 3 credits        | $0.010            |

### RevenueCat Integration (Mobile)

1. RevenueCat SDK initialized in KMP shared module at app startup
2. Paywall screen uses RevenueCat Paywalls SDK for no-code A/B testable paywall UI
3. Purchase → RevenueCat validates with App Store / Play Store → webhook to API Gateway
4. API Gateway webhook handler: verify RevenueCat signature → update `subscriptions` table → grant credits
5. Subscription status synced on every app foreground event via RevenueCat SDK

### Stripe Integration (Web)

1. Stripe Checkout for initial subscription purchase on web
2. Stripe Customer Portal for subscription management (upgrade, downgrade, cancel)
3. Stripe Webhook → API Gateway: handle `subscription.created`, `subscription.deleted`, `invoice.paid`
4. Web subscriptions use the same credits model — unified `subscriptions` table

### Credit Gate Logic

```
User sends message
        │
        ▼
Does user have enough credits?
        │
   Yes  │  No
        │   └──→  Return 402 error with upgrade prompt
        │          Client shows paywall / top-up screen
        ▼
Check subscription tier → determine model access
        │
        ▼
Call model → deduct credits on stream completion
```

### Connections Established

```
Mobile App (KMP)   →  RevenueCat SDK                    [Native SDK — purchase, get entitlements]
RevenueCat         →  API Gateway /webhooks/revenuecat   [HTTPS webhook — subscription events]
Web Dashboard      →  Stripe Checkout / Portal           [HTTPS redirect — hosted payment pages]
Stripe             →  API Gateway /webhooks/stripe       [HTTPS webhook — subscription events]
API Gateway        →  subscriptions table (Supabase)     [Update credits, tier, status]
```

### Go / No-Go Criteria

- [ ] Subscription purchase on iOS works end-to-end (TestFlight)
- [ ] Subscription purchase on Android works (Play Store internal track)
- [ ] Stripe web purchase works
- [ ] Credits deducted correctly after each operation
- [ ] Free tier users are blocked when credits are exhausted
- [ ] RevenueCat and Stripe webhooks update the same `subscriptions` table correctly

---

## Phase 9 — Memory, Persistence & File Storage (Weeks 10–11) ✅ DONE

> Make the agent feel like it truly knows the user over time. Cross-conversation memory, persistent file storage, and conversation search complete the product experience.

### Memory Architecture (Two Layers)

```
Short-term memory (within conversation)
────────────────────────────────────────
• Last 20 messages fetched from messages table
• Injected as conversation history into OpenClaw context
• Automatically summarized when approaching context limit

Long-term memory (cross-conversation)
──────────────────────────────────────
• Markdown files per user stored in Supabase Storage
  Path: user-memory/{user_id}/*.md
• Loaded into every new sandbox at /workspace/memory/
• Synced back to Supabase Storage on sandbox pause/destroy
• Example memory files:
  - personal.md     → "User's name is Ahmed, works at Microsoft SwiftKey"
  - preferences.md  → "User prefers concise answers, uses Kotlin"
  - projects.md     → "User has SaaS products: PhotoAI Generator, Signal Whisper"
```

### Memory Management UI

Settings screen shows:

- "You have 47 facts stored across 3 memory files"
- Ability to view, edit, and delete individual memory entries
- Option to clear all memory

### File Storage Structure

```
Supabase Storage Buckets:

user-files/{user_id}/          ← Agent-created output files
user-uploads/{user_id}/        ← Files uploaded by the user
user-memory/{user_id}/         ← Agent memory Markdown files
```

- **Signed URLs**: all file access uses signed URLs with 24-hour expiry, regenerated on access
- **Storage quotas**: enforced per subscription tier (5 GB Free → 100 GB Power), checked before upload
- **File listing**: `GET /files` returns all files across all conversations, sortable by date/type

### Conversation Search

- Full-text search on `messages` table using PostgreSQL `tsvector` / GIN index
- Endpoint: `GET /conversations/search?q=query` — returns matching conversations + message previews
- Search UI on all platforms: search bar in conversation list, highlights matching messages in results

### Connections Established

```
OpenClaw Memory  →  Supabase Storage     [Sync memory files on sandbox lifecycle events]
API Gateway      →  Supabase Storage     [Upload/download user files, generate signed URLs]
Android/iOS/Web  →  API Gateway /files   [File listing, search, download URL generation]
```

---

## Phase 10 — Polish, Testing & Launch (Weeks 12–14) ✅ DONE

### Testing Checklist

**Unit Tests**

- [ ] All shared KMP repository functions (mocked API)
- [ ] All API Gateway endpoints (pytest)
- [ ] Credit deduction logic
- [ ] Model routing logic

**Integration Tests**

- [ ] Full message → agent → tool → response flow in test E2B sandbox
- [ ] BYOK key encryption/decryption round trip
- [ ] Subscription webhook handling (RevenueCat + Stripe)

**E2E Tests**

- [ ] Maestro scripts: onboarding, send message, subscribe, BYOK setup (Android + iOS)
- [ ] Playwright scripts: web dashboard critical paths

**Load Testing**

- [ ] k6: 100 concurrent users, verify sandbox scaling and API rate limits
- [ ] Verify E2B sandbox auto-scale (check concurrent sandbox limit on your E2B plan)

**Security Testing**

- [ ] Sandbox isolation: User A cannot access User B's sandbox or files
- [ ] BYOK keys not present in any logs
- [ ] JWT tokens expire and refresh correctly
- [ ] Rate limiting prevents credit exhaustion attacks

### App Store Submission Checklist

**iOS (App Store)**

- [ ] Screenshots for all device sizes (iPhone 6.7", 6.1", iPad Pro)
- [ ] Privacy manifest (`PrivacyInfo.xcprivacy`) — required iOS 17+
- [ ] App Privacy nutrition labels filled out
- [ ] Review notes: explain agent execution happens server-side (app is UI only)
- [ ] Demo video showing: code execution, web browsing, file creation

**Android (Google Play)**

- [ ] Screenshots for phone + tablet
- [ ] Content rating questionnaire
- [ ] Data safety form
- [ ] 7-day pre-launch report (automated testing)

### Launch Infrastructure

| Service  | Config                                       | Purpose                         |
| -------- | -------------------------------------------- | ------------------------------- |
| Vercel   | Custom domain, edge functions                | Web dashboard, global CDN       |
| Railway  | Auto-scaling: min 2, max 20 workers          | API Gateway scaling             |
| E2B      | Pro plan (100+ concurrent sandboxes)         | Agent sandboxes                 |
| Supabase | Point-in-time recovery, PgBouncer, RLS audit | Database reliability            |
| Sentry   | Error tracking (API + mobile + web)          | Error monitoring                |
| PostHog  | Product analytics                            | Usage tracking, funnel analysis |

### Key Metrics to Track at Launch

- **Activation**: % of signups who complete first agent task
- **Retention D7 / D30**: % of users who return after 7 and 30 days
- **Conversion**: Free → Paid conversion rate
- **Credits per user**: Average credits consumed per active user per day
- **Sandbox errors**: E2B sandbox creation failures and timeout rates
- **Gross margin**: Revenue per user minus COGS per user

---

# 3. Complete Tech Stack & Services

| Layer                | Technology                  | Purpose                                  | Est. Monthly Cost        |
| -------------------- | --------------------------- | ---------------------------------------- | ------------------------ |
| Mobile (Android)     | Kotlin + Jetpack Compose    | Native Android UI                        | $0                       |
| Mobile (iOS)         | Kotlin/Native + SwiftUI     | Native iOS UI via KMP interop            | $0 (+ $99/yr Apple Dev)  |
| Shared Logic         | Kotlin Multiplatform + Ktor | Business logic, API client, SSE parser   | $0                       |
| Web Frontend         | Next.js 15 + Tailwind CSS   | Web dashboard UI                         | $0–$20 (Vercel hobby)    |
| API Gateway          | FastAPI (Python 3.12)       | Backend API, streaming, orchestration    | $5 (Railway starter)     |
| Agent Engine         | OpenClaw (MIT)              | AI agent framework inside sandboxes      | $0                       |
| Sandboxes            | E2B (Firecracker microVMs)  | Isolated per-user execution environments | $150+ (usage-based)      |
| AI Model (bundled)   | Anthropic Claude API        | Haiku / Sonnet / Opus                    | $2–30 per active user/mo |
| Database + Auth      | Supabase (PostgreSQL)       | All data, user auth, RLS, file storage   | $0–25 (Pro plan)         |
| Secret Management    | Supabase Vault              | Encrypted BYOK key storage               | Included in Supabase     |
| File Storage         | Supabase Storage            | Agent output files, user uploads, memory | $0.021/GB/mo             |
| Mobile Subscriptions | RevenueCat                  | In-app purchase management, A/B paywalls | $0 under $2.5k MRR       |
| Web Subscriptions    | Stripe                      | Web subscription billing, invoicing      | 2.9% + $0.30/transaction |
| Monitoring           | Sentry + PostHog            | Error tracking + product analytics       | $0–26                    |
| Search (agent)       | Brave Search API / SerpAPI  | Web search tool for agent                | $3–9/mo for 1k queries   |
| CI/CD                | GitHub Actions              | Tests, builds, deploy on merge           | $0 (public)              |

---

# 4. Unit Economics & Cost Model

## Cost Per User Per Month (Starter Tier, $9.99)

| Cost Item                     | Assumption                                      | Monthly Cost/User |
| ----------------------------- | ----------------------------------------------- | ----------------- |
| Claude Haiku (80% of calls)   | 200 avg calls × 600 tokens × $1/M in + $5/M out | $0.84             |
| Claude Sonnet (18% of calls)  | 45 calls × 600 tokens × $3/M in + $15/M out     | $0.54             |
| E2B sandbox compute           | 20 active sessions × 5 min × $0.000028/sec      | $0.17             |
| Supabase storage + DB         | Allocated per user (100 MB avg)                 | $0.12             |
| API Gateway infra (Railway)   | Allocated per user                              | $0.15             |
| **Total COGS**                |                                                 | **$1.82**         |
| Revenue (after Apple 30% cut) | $9.99 × 0.70 = $6.99                            | $6.99             |
| **Gross Margin (mobile)**     |                                                 | **$5.17 (74%)**   |
| Revenue (web via Stripe 3%)   | $9.99 × 0.97 = $9.69                            | $9.69             |
| **Gross Margin (web)**        |                                                 | **$7.87 (81%)**   |

## Cost Optimization Strategies

1. **Prompt caching** — Enable Anthropic prompt caching on system prompt + conversation history. Cache hits cost 0.1× base price. This alone cuts LLM cost by ~50% for active users.
2. **Model routing** — 80% Haiku, 18% Sonnet, 2% Opus. Do not default to Sonnet for simple queries.
3. **Sandbox idle management** — Auto-pause after 10 minutes. A user who sends 10 messages/day actually only needs the sandbox active for ~15 minutes/day = $0.025/day.
4. **Batch processing** — Non-urgent background tasks (memory sync, file indexing) use Anthropic batch API at 50% discount.
5. **Web subscriptions** — Encourage web signups to bypass Apple's 30% cut. Even a 30% shift to web dramatically improves blended margin.

## Break-Even Analysis

| Active Users | Monthly Revenue | Monthly COGS | Monthly Margin |
| ------------ | --------------- | ------------ | -------------- |
| 100          | $750            | $285         | $465           |
| 1,000        | $7,500          | $2,300       | $5,200         |
| 10,000       | $75,000         | $19,000      | $56,000        |
| 50,000       | $375,000        | $85,000      | $290,000       |

_Assumes 5% free-to-paid conversion, blended $7.50 ARPU after stores, avg $1.90 COGS/user_

---

# 5. 14-Week Timeline

| Week | Phase                  | Milestone                                                                    |
| ---- | ---------------------- | ---------------------------------------------------------------------------- |
| 1    | P1: Backend Foundation | Supabase DB, Railway FastAPI, auth middleware, CRUD endpoints                |
| 2    | P1–P2                  | Claude API integrated, SSE streaming tested end-to-end via curl              |
| 3    | P2–P3                  | KMP project setup, shared module, Android app chatting with backend          |
| 4    | P3                     | iOS app functional, streaming UI on both platforms, conversation persistence |
| 5    | P3–P4                  | E2B integration, first OpenClaw sandbox created and receiving messages       |
| 6    | P4–P5                  | Code execution working, agent writes and runs Python scripts                 |
| 7    | P5                     | Web browse, web search, file creation tools all working end-to-end           |
| 8    | P6                     | Next.js web dashboard with full chat parity, Supabase auth on web            |
| 9    | P7                     | BYOK flow complete: key encryption, vault storage, multi-provider support    |
| 10   | P8                     | RevenueCat + Stripe integrated, subscription tiers enforced, credit gates    |
| 11   | P9                     | Memory persistence cross-conversation, file storage, conversation search     |
| 12   | P10: Testing           | All unit + integration + E2E tests passing, load test complete               |
| 13   | P10: Beta              | TestFlight + Play Store internal track live, 50 beta users, feedback done    |
| 14   | P10: Launch            | App Store + Play Store public launch, web app live, Product Hunt launch      |

---

# 6. Risks & Mitigations

| Risk                                          | Likelihood | Impact   | Mitigation                                                                                                                                              |
| --------------------------------------------- | ---------- | -------- | ------------------------------------------------------------------------------------------------------------------------------------------------------- |
| E2B sandbox cold start > 5 sec                | Medium     | High     | Pre-warm sandboxes on login. Keep active users' sandboxes running. Show progress indicator during creation.                                             |
| Anthropic API costs exceed revenue at scale   | Medium     | High     | Aggressive model routing (80% Haiku). Prompt caching enabled from Day 1. Hard credit gates. BYOK tier offloads heavy users.                             |
| App Store rejection (agent executes code)     | Medium     | High     | All execution is server-side — the app is UI only. Prepare detailed review notes explaining this. QuickClaw and similar apps are approved precedent.    |
| User isolation breach (sandbox escape)        | Low        | Critical | E2B uses Firecracker (same tech as AWS Lambda). Each user gets a separate microVM. Network policy blocks inter-VM traffic. Regular penetration testing. |
| OpenClaw project abandoned or license changed | Low        | High     | MIT license means we can fork at any time. The codebase is self-contained. Maintain an internal fork as fallback.                                       |
| BYOK key stored insecurely                    | Low        | Critical | AES-256-GCM in Supabase Vault. Key never logged anywhere. Security audit in Phase 10.                                                                   |
| Stripe / RevenueCat compliance issues         | Medium     | Medium   | Follow Stripe prohibited items policy. No financial advice in agent system prompt. Proactive disclosure in listings.                                    |
| KMP iOS interop issues                        | Medium     | Medium   | Use well-tested KMP patterns. Keep complex UI 100% in SwiftUI. Only share business logic. Budget extra week for iOS polishing.                          |
| Heavy users becoming unprofitable             | Medium     | Medium   | Credit caps per tier. Monitor P99 usage. BYOK tier for power users. Hard rate limits on tool calls per hour.                                            |

---

_Total estimated time to MVP (P1–P5): 7 weeks. Total to production launch: 14 weeks._

_Built on OpenClaw (MIT License) · Sandboxed by E2B · Powered by Anthropic Claude_
