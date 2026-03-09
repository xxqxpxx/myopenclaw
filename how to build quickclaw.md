# How to build QuickClaw: an AI agent app with zero setup

**QuickClaw and its competitors are thin iOS wrappers around OpenClaw, the open-source AI agent framework with 247,000+ GitHub stars.** The architecture is straightforward: a native SwiftUI chat interface sends messages to a per-user cloud sandbox running the OpenClaw agent process, which orchestrates LLM calls (primarily Claude), code execution, web browsing, and file management—all without the user touching a single API key. The developer bundles their own Anthropic API credentials server-side and monetizes through credit-based subscriptions ranging from $5.99 to $199.99. Replicating this product requires roughly 8 weeks, five key services (E2B sandboxes, Anthropic API, Supabase, RevenueCat, Railway), and approximately **$285/month at 100 users scaling to $42,000/month at 50,000 users**.

---

## OpenClaw is the engine behind the entire ecosystem

Every product in this space—QuickClaw, MaxClaw, MyClaw, Clowd.bot, OpenClawd—is built on **OpenClaw**, the open-source AI agent framework created by Peter Steinberger (founder of PSPDFKit). Originally named Clawdbot, then Moltbot (renamed after Anthropic trademark complaints), OpenClaw became the fastest-growing open-source project in GitHub history, surpassing React's all-time star count by March 2026 with **277,000+ stars** and 52,000+ forks under the MIT license.

OpenClaw's architecture has five components that make it uniquely suited for wrapping into a consumer product. The **Gateway** is a long-lived WebSocket server (default port 18789) that routes messages from 20+ chat channels including WhatsApp, Telegram, Slack, Discord, Signal, and iMessage. The **Brain** orchestrates LLM calls using a ReAct (Reasoning + Acting) agent loop: input → context → model → tools → repeat → reply. **Memory** persists as local Markdown files on disk. **Skills** are modular plug-in capabilities defined in YAML frontmatter files, with 13,700+ community skills on the ClawHub registry. The **Heartbeat** is a configurable scheduler that proactively wakes the agent to execute tasks.

The framework is model-agnostic—it supports Claude, GPT, Gemini, DeepSeek, and local models via Ollama—but QuickClaw specifically ships with **Claude (Anthropic)** pre-configured. OpenClaw's agent capabilities include shell command execution, browser automation via Chrome DevTools Protocol, file read/write, email and calendar management, web browsing, multi-agent spawning, and voice input/output. Steinberger announced joining OpenAI on February 14, 2026, with the project moving to an open-source foundation.

---

## QuickClaw wraps OpenClaw into a 17.3 MB native iOS app

QuickClaw launched **February 9, 2026** by developer Max Hansen (copyright: Max Blade). The App Store listing explicitly states: "QuickClaw is the easiest way to use OpenClaw, the open-source AI agent framework." At just **17.3 MB**, the app is clearly a thin client—the real computing happens in the cloud.

The app requires iOS 18.0+ and runs on iPhone, iPad, Mac (M1+), and Apple Vision Pro. It currently holds a **2.3/5 rating** from only 3 reviews, though one $200-tier subscriber called it "a beast" that "improved my business in 3 days." The developer's only other app is "Talk Macros," a macro tracking utility, suggesting this is a solo indie developer project.

QuickClaw uses a **credit-based pricing system** with six tiers:

| Tier | Price | Likely Structure |
|------|-------|-----------------|
| Quick Claw | $5.99 | Entry subscription or credit pack |
| Quick Claw | $24.99 | Monthly subscription |
| Power Claw | $17.99 | Mid-tier credit pack |
| Power Claw | $74.99 | Monthly subscription |
| Beast Claw | $199.99 | Premium unlimited tier |
| Top-up | $49.99 | 2,800 credits one-time |

The "no API key" experience works because **QuickClaw bundles its own Anthropic API credentials on the server side**. Users pay credits that abstract away per-token costs. Each user gets what the app calls "your own private, isolated workspace in the cloud"—a dedicated OpenClaw instance where conversations, files, and agent memory are segregated from other users.

Notable capabilities include writing documents and code (delivered as real files), autonomous web browsing, reminders and wake-up calls with custom briefings, email and calendar management, file creation and organization, multi-step autonomous task execution, cross-conversation context memory, and API calling. The marketing explicitly positions it against "clunky web wrappers"—it is a native iOS application.

---

## Five competitors reveal divergent business models for the same core technology

The "Claw ecosystem" has fragmented into dozens of wrappers around OpenClaw, each taking a different approach to monetization and technical architecture. Here is what the research found on each competitor:

**MiniMax MaxClaw ($19/month)** is the most well-resourced competitor, built by MiniMax—one of China's "Six AI Tigers" with a $2.5B valuation, 212M+ global users, and a January 2026 Hong Kong IPO. MaxClaw launched February 25, 2026 at agent.minimax.io. Its key differentiator: it uses **MiniMax's own M2.5 model** (229 billion parameters, mixture-of-experts, 200K–1M token context) instead of Claude or GPT, claiming costs **1/7 to 1/20 of Claude 3.5 Sonnet per token**. It provides 200 free daily credits, 10,000+ pre-configured "Experts," and zero API key management. Each agent runs in a strictly isolated container with encrypted storage. The subscription includes all model usage with no per-token charges.

**MyClaw.ai ($19–79/month)** takes a pure infrastructure approach. Launched February 5, 2026, it provides a dedicated OpenClaw instance running 24/7 in an isolated container, but **requires users to bring their own API keys** (Anthropic, OpenAI, Google, or local models via Ollama). This means MyClaw handles DevOps—containers, backups, updates, security patches—while users pay AI providers directly. The company explicitly disclaims any affiliation with the OpenClaw project. Contact email (jason@flot.ai) suggests ties to Flot AI.

**Clowd.bot ($0.50 + per-token)** offers the most innovative pricing model. Powered by ATXP (Agent Transaction Protocol) from Circuit & Chisel Inc., it charges a **$0.50 one-time launch fee** per instance with no subscription, no hourly compute fees, and no minimums. Users pay only for LLM token consumption at competitive rates. Built-in access to Claude, GPT, and Gemini means no API keys needed. Instance specs are modest: 2 vCPU, 2 GB RAM, 1 GB storage. When idle, costs are zero. This is the lowest barrier to entry in the ecosystem.

**OpenClawd.ai ($19.99–199.99/month)** warrants caution. Despite using the "Formerly Clawdbot & Moltbot" tagline identical to the real OpenClaw project, **OpenClawd is an independent third-party platform with no official connection to OpenClaw or Peter Steinberger**. It offers cloud hosting with a credits-based system: Starter ($19.99/mo, 40,000 credits, MiniMax M2.5 only), Professional ($39.99/mo, 80,000 credits, adds Gemini 2.5 Pro), and Ultimate ($199.99/mo, 400,000 credits, all models including Claude and GPT). The site uses stock photo testimonials (pravatar.cc URLs visible in source code), and no clear company entity is identified—branded only as "built by the community." This suggests a potentially misleading operation trading on OpenClaw's brand recognition.

| Feature | QuickClaw | MaxClaw | MyClaw.ai | Clowd.bot | OpenClawd.ai |
|---------|-----------|---------|-----------|-----------|-------------|
| **Price** | $5.99–$199.99 credits | $19/mo flat | $19–79/mo | $0.50 + tokens | $19.99–199.99/mo |
| **API keys needed** | No | No | Yes (BYOK) | No | No (cloud) |
| **AI model** | Claude | MiniMax M2.5 | User's choice | All major LLMs | Tiered by plan |
| **Platform** | iOS native app | Web app | Web dashboard | Web terminal | Web + CLI |
| **Isolation** | Cloud workspace | Isolated container | Isolated container | Secure environment | Cloud container |
| **Legitimacy** | Indie dev, new | Major AI company | Transparent indie | Clear branding | Suspicious branding |

---

## The technical architecture follows a thin-client pattern with Firecracker microVMs

The architecture of QuickClaw-style apps resolves into three layers: a native iOS front-end, a backend API server, and per-user sandboxed agent environments.

**The iOS app is a thin SwiftUI client** that sends user messages via HTTP POST and receives streamed AI responses via Server-Sent Events (SSE). At 17.3 MB, QuickClaw contains minimal logic—the app handles authentication, message display with token-by-token rendering, file previews, and subscription management via RevenueCat. SSE is the clear winner over WebSockets for this use case because AI chat is fundamentally unidirectional streaming: the user sends a prompt, then the server streams tokens back. SSE uses standard HTTP, works through all proxies and CDNs, has automatic reconnection, and is what ChatGPT and Claude use for their streaming interfaces.

The SwiftUI implementation requires a streaming message view that renders Markdown and code blocks token-by-token, a composer view with attachments, and a lazy-loading conversation list. Apple's `URLSession` with async/await natively supports SSE:

```swift
let (bytes, _) = try await URLSession.shared.bytes(for: request)
for try await line in bytes.lines {
    if line.hasPrefix("data: ") {
        let token = String(line.dropFirst(6))
        await MainActor.run { self.responseText += token }
    }
}
```

**The backend API server** (Python/FastAPI or Node.js) coordinates between the iOS client, LLM providers, and sandboxes. It holds all API keys server-side, handles authentication via Supabase Auth with JWT tokens, enforces rate limits and credit tracking, and routes requests to the appropriate user sandbox.

**Per-user isolation uses Firecracker microVMs**, the same technology behind AWS Lambda. The industry standard is **E2B (e2b.dev)**, used by 88% of Fortune 100 companies for AI agent sandboxes. Each user session gets a dedicated Firecracker microVM with its own Linux kernel, memory space, and filesystem—booting in under **200ms** with less than 5 MiB overhead. Inside each microVM runs an OpenClaw Gateway process, the user's workspace directory, and conversation history. Sandboxes can be paused and resumed via VM snapshotting for cost efficiency.

E2B pricing makes this viable at scale: **$0.000028/second for a 2-vCPU sandbox** (~$0.10/hour). A typical 5-minute sandbox session costs $0.0084. At 10 sessions per user per day, that's roughly **$2.52/user/month** for compute isolation alone. E2B's Hobby tier is free with $100 in credits; Pro is $150/month with up to 24-hour sessions and 100 concurrent sandboxes.

Alternatives to E2B include Fly.io Machines (Firecracker microVMs with global edge regions), Modal (sub-second cold starts, GPU support), Vercel Sandbox (integrated with Next.js), and self-hosted Firecracker on Kubernetes (maximum control, massive engineering effort). For an MVP, E2B is the clear choice.

---

## A step-by-step build guide in eight weeks

**Phase 1: Foundation (Weeks 1–2).** Set up a Supabase project with tables for users, conversations, messages, and usage tracking. Deploy a FastAPI backend on Railway ($5/month). Implement Supabase Auth with JWT token passing from iOS. Create the SwiftUI project with basic chat UI scaffolding using LazyVStack in ScrollView. Configure RevenueCat with App Store Connect products.

**Phase 2: Core AI integration (Weeks 3–4).** Integrate the Anthropic API in the backend with SSE streaming via FastAPI's `StreamingResponse`. Build iOS-side streaming with `URLSession.shared.bytes`. Add conversation persistence in Supabase PostgreSQL. Implement intelligent model routing—**80% of simple queries go to Claude Haiku ($1/M input tokens) while complex tasks escalate to Sonnet ($3/M) or Opus ($5/M)**—reducing costs by 60–70%. Add per-user token counting and tier-based limits.

**Phase 3: Sandboxed agent execution (Weeks 5–6).** Integrate E2B's Python SDK to create per-session sandboxes. Define tool schemas for code execution, file operations, and web browsing. Build the agent loop using either OpenClaw's native ReAct architecture or LangGraph (GA since May 2025, used in production by ~400 companies including LinkedIn and Uber). Display code blocks, execution output, and generated files in the iOS chat view. Implement sandbox lifecycle management—create on task start, snapshot on idle, destroy on timeout.

**Phase 4: Monetization and launch (Weeks 7–8).** Implement RevenueCat paywall with three tiers. Gate expensive operations (Opus model, extended sandbox sessions) behind subscription checks. Add rate limiting per user and per tier. Comprehensive error handling for network failures, sandbox timeouts, and LLM errors. TestFlight beta distribution, then App Store submission.

The complete tech stack:

| Layer | Service | Monthly Cost |
|-------|---------|-------------|
| iOS frontend | SwiftUI + Swift 6 | $99/year (Apple Developer) |
| Streaming protocol | SSE (Server-Sent Events) | Free |
| Backend API | FastAPI on Railway | $5–50 |
| Agent framework | OpenClaw or LangGraph | Free (open-source) |
| Primary LLM | Anthropic Claude | $2–30 per active user |
| Code sandbox | E2B Firecracker microVMs | $2.52 per active user |
| Database & auth | Supabase PostgreSQL | $0–25 |
| Subscriptions | RevenueCat | Free under $2,500 MRR |
| Monitoring | LangSmith + Sentry | $0–39 |

---

## Unit economics determine whether this business can survive

The critical financial constraint is that **Apple takes 30% of all in-app subscription revenue**, leaving $10.49 from a $14.99/month Pro subscriber. Against that, per-user costs include LLM API calls ($4–6/month with model routing), E2B sandbox compute ($2–3/month), and allocated backend/database costs ($0.50–1.50/month). This yields a **gross margin of roughly 10–40% per user** at moderate usage, with heavy users potentially becoming unprofitable.

The path to viability requires several optimizations. **Prompt caching** reduces repeated context costs by 90% (Anthropic charges 0.1x the base price for cache hits). **Batch API processing** for background tasks saves 50%. **Aggressive model routing** sends the vast majority of queries to Haiku-class models at $1/M tokens instead of Sonnet at $3/M. At scale, offering web-based subscriptions through Stripe (bypassing Apple's 30% cut) dramatically improves margins.

Projected break-even sits at approximately **5,000–10,000 users with a 5% free-to-paid conversion rate** at $14.99/month. The AI wrapper market is estimated at $50 billion and growing 3x faster than traditional SaaS, but competition is fierce—roughly 77 new AI wrapper products launch weekly, with only 10–15% achieving product-market fit within 12 months. The key defensibility plays are proprietary skills/integrations, accumulated user data and personalization, and vertical specialization rather than trying to be a generic agent.

## Conclusion

Building a QuickClaw clone is technically straightforward—the entire core technology is open-source (OpenClaw, MIT license) and the infrastructure services (E2B, Supabase, RevenueCat) are mature and well-documented. The real challenge is economic, not technical. With Apple's 30% cut, per-user LLM costs of $2–6/month, and sandbox compute on top, margins are razor-thin unless you implement aggressive model routing, prompt caching, and usage caps. The most interesting strategic insight from the competitive landscape: **Clowd.bot's pure pay-per-token model with a $0.50 launch fee may be more sustainable than subscription models**, since it perfectly aligns costs with revenue and eliminates the heavy-user subsidy problem. MaxClaw's approach of using its own cheaper model (MiniMax M2.5 at 1/7th to 1/20th of Claude's cost) offers another path—but requires being an AI company, not just a wrapper. For an indie developer, the QuickClaw approach of credit-based pricing with tiered subscriptions remains the most practical starting point, with the understanding that unit economics improve significantly only at scale with caching, routing, and eventually a web payment option to escape Apple's commission.