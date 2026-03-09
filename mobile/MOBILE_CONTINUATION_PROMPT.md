# Mobile KMP Continuation Prompt

Use this prompt to continue the KMP mobile refactor in a separate session.

---

## Context

We're building **myOpenClaw** — an AI agent app (QuickClaw/OpenClaw clone). The full plan is at `/Users/ahmed/Documents/openclaw/quickclaw-plan.md`.

The **backend** (FastAPI) is already built at `/Users/ahmed/Documents/openclaw/backend/` with these endpoints:

- `POST /api/v1/conversations/` — create conversation
- `GET /api/v1/conversations/` — list conversations
- `DELETE /api/v1/conversations/{id}` — delete conversation
- `GET /api/v1/conversations/{id}/messages` — list messages
- `POST /api/v1/conversations/{id}/chat/stream` — SSE streaming chat
- `GET /api/v1/users/me` — user profile
- `GET /api/v1/users/me/credits` — credit balance

The SSE event format is:

```
data: {"type":"token","content":"Hello ","model":"claude-haiku-4-5"}
data: {"type":"tool_start","tool":"code_execute","input":{"code":"print('hello')"}}
data: {"type":"tool_result","tool":"code_execute","output":"hello"}
data: {"type":"file","filename":"report.pdf","url":"...","size":45000}
data: {"type":"done","total_tokens":1204,"credits_used":3}
data: {"type":"error","error":"..."}
```

The **KMP mobile app** is at `/Users/ahmed/Documents/openclaw/mobile/KMP-App-Template-main/`. It was cleaned from an old app called "Signalwhisper". We already:

1. **Renamed package** `com.signalwhisper` → `com.myopenclaw` (all dirs, all source, build configs, iOS config)
2. **Renamed classes**: `SignalWhisperApplication` → `MyOpenClawApplication`, `SignalWhisperDatabase` → `MyOpenClawDatabase`, `SignalwhisperTheme` → `MyOpenClawTheme`
3. **Created new Chat models** at `domain/models/Chat.kt` — `MessageRole`, `SSEEventType`, `Conversation`, `Message`, `SSEEvent`, `CreateConversationRequest`, `SendMessageRequest`, `CreditBalance`, `ChatUiMessage`
4. **Created `ConversationRepository`** interface at `domain/repository/ConversationRepository.kt`
5. **Created `ConversationRepositoryImpl`** at `data/repository/ConversationRepositoryImpl.kt`
6. **Rewrote `ApiConfig.kt`** — BASE_URL now `http://10.0.2.2:8000`, STREAM_TIMEOUT=300s, deep links use `myopenclaw://`
7. **Rewrote `ApiService.kt`** — new endpoints for conversations CRUD, SSE streaming via Ktor ByteReadChannel, credit balance
8. **Created `ChatViewModel.kt`** — full SSE streaming state machine with `ChatState`, handles token/tool_start/tool_result/done/error events
9. **Created `ConversationListViewModel.kt`** — loads/deletes conversations
10. **Created `ChatScreen.kt`** — Compose UI with message bubbles, streaming indicator, tool badges, input bar
11. **Created `ConversationListScreen.kt`** — list with FAB for new chat, swipe delete

## What's Left (Your Tasks)

### 1. Fix Dead References in AppModule.kt

The file at `composeApp/src/commonMain/kotlin/com/myopenclaw/di/AppModule.kt` still imports **deleted** market-specific classes. These imports and their corresponding Koin bindings must be removed:

- All `marketdata.*` imports (use cases, viewmodels, repository)
- `MarketDataRepository`, `MarketDataRepositoryImpl`
- `SignalsRepository`, `SignalsRepositoryImpl`, `SignalsViewModel`, `CheckMarketSignalAccessUseCase`
- `AnalysisRepository`, `AnalysisRepositoryImpl`, `AnalysisHistoryViewModel`
- `WatchlistRepository`, `WatchlistRepositoryImpl`, `WatchlistViewModel`, `StockSearchViewModel`, `StockSearchRepository`, `StockSearchRepositoryImpl`
- `AlertStore`, `AlertViewModel`
- `AnalyzeChartUseCase`, `SendChatMessageUseCase`, `TradeIdeasViewModel`, `ChartAnalysisViewModel`
- `MarketsViewModel`, `SearchViewModel`, `SignalDetailsViewModel`
- The entire `marketDataModule`, `signalsModule`, `analysisModule`, `watchlistModule` modules
- Remove them from `appModules()` list

Then **add** a new `chatModule`:

```kotlin
val chatModule = module {
    single<ConversationRepository> {
        ConversationRepositoryImpl(apiService = get())
    }
    viewModel { ChatViewModel(conversationRepository = get()) }
    viewModel { ConversationListViewModel(conversationRepository = get()) }
}
```

And add `chatModule` to `appModules()`.

### 2. Fix Dead References in NavGraph.kt

The file at `composeApp/src/commonMain/kotlin/com/myopenclaw/ui/navigation/NavGraph.kt` still imports deleted screens:

- Remove imports for: `MarketsScreen`, `InsightsScreen`, `WatchlistScreenNew`, `AddStockScreen`, `WatchlistViewModel`, `marketdata.*`, `InsiderTradingListScreen`, `CongressionalTradingListScreen`, `OptionsFlowListScreen`, `AIChatScreen`, `SignalsViewModel`, `SignalsState`, `MarketSignalAccess`, `AnalysisHistoryViewModel`, `AnalysisState`, `ChartAnalysisViewModel`
- Remove all `Screen` sealed class entries for: `Markets`, `Insights`, `Watchlist`, `AddStock`, `AlertSettings`, `SocialSentiment`, `DarkPool`, `GovernmentContracts`, `LobbyistActivity`, `PoliticalDonations`, `InsiderTrading`, `CongressTrading`, `OptionsFlow`, `AIChat`, `AITradeIdeas`, `AnalysisHistory`, `ChartAnalysis`, `ChartAnalysisResults`, `MarketSignals`, `HowSignalsWork`, `SignalDetails`, `Search`, `StockDetails`
- Remove those routes from `screensWithBottomNav` and from `composable()` blocks
- **Add** new routes:
  - `Screen.Conversations` → `ConversationListScreen`
  - `Screen.Chat` (with optional `conversationId` arg) → `ChatScreen`
- Update bottom nav to: Home, Conversations, Profile
- The `Home` screen can be simplified to show a "New Chat" CTA and recent conversations

### 3. Fix BottomNavigationBar.kt

Update the items to reflect the new app: Home, Conversations, Profile (remove Markets, Insights, Watchlist)

### 4. Fix HomeViewModel.kt

It likely imports deleted market data use cases. Simplify it — it should just show recent conversations or a welcome message.

### 5. Fix HomeScreen.kt / HomeScreenNew.kt / DashboardScreen.kt

These contain market-specific UI. Simplify or replace with a clean home screen that shows:

- Welcome message
- "New Chat" button
- Recent conversations list
- Credit balance

### 6. Clean Up Remaining Dead Files

Run `grep -rl "MarketData\|InsiderTrad\|CongressTrad\|DarkPool\|OptionsFlow\|AnalysisHistory\|TradingIdea\|ScreenerResult\|AlertStore\|ChartAnalysis\|SignalsRepository\|WatchlistRepository" --include="*.kt" composeApp/src/` to find any remaining files with dead references and fix them.

### 7. Remove `domain/usecase/ai/SendChatMessageUseCase.kt`

This old use case references deleted `MarketDataRepository`. We no longer need thin use cases for chat — the `ChatViewModel` calls `ConversationRepository` directly.

### 8. Verify Build

After all fixes, try `./gradlew composeApp:compileKotlinAndroid` (or equivalent) to check for compile errors. Fix any remaining issues.

### Key Files to Reference

- Backend schemas: `/Users/ahmed/Documents/openclaw/backend/app/models/schemas.py`
- Backend streaming endpoint: `/Users/ahmed/Documents/openclaw/backend/app/api/conversations.py`
- Full plan: `/Users/ahmed/Documents/openclaw/quickclaw-plan.md` (Phase 3 section)
