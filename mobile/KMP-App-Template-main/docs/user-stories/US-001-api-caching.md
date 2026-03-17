# User Story: API Caching & Offline-First Data Strategy

**Story ID:** US-001
**Priority:** High
**Estimated Complexity:** Large
**Sprint:** TBD

---

## User Story

**As a** myOpenClaw user,
**I want** the app to show my market data instantly when I open it,
**So that** I can quickly view my trading signals and market intelligence without waiting for network requests.

---

## Background

Currently, all data is fetched fresh from the API every time a screen is opened. This results in:
- Loading spinners on every screen transition
- Poor user experience on slow networks
- No offline access to previously viewed data
- Repeated API calls for unchanged data

---

## Acceptance Criteria

### AC-1: Instant Data Display
- [ ] When opening any market data screen, cached data displays immediately (< 100ms)
- [ ] Loading indicator shows only for fresh data fetch, not initial display
- [ ] Cached data shows with a subtle "updating..." indicator while refreshing

### AC-2: Background Data Refresh
- [ ] After showing cached data, app automatically fetches fresh data from API
- [ ] UI seamlessly updates when new data arrives
- [ ] If API call fails, cached data remains visible with optional retry button

### AC-3: Cache Freshness Policies
- [ ] Each data type has appropriate cache expiration (see Technical Specs)
- [ ] Expired cache still displays while fetching (stale-while-revalidate)
- [ ] User can force refresh via pull-to-refresh gesture

### AC-4: Offline Mode
- [ ] App functions with cached data when offline
- [ ] Clear offline indicator shown to user
- [ ] Graceful degradation for features requiring real-time data

### AC-5: Cache Management
- [ ] Cache is cleared on user logout
- [ ] User can manually clear cache from settings
- [ ] Cache size is reasonable (< 50MB recommended)

---

## Data Types to Cache

### High Priority (Frequently Accessed)

| Data Type | Endpoint | Cache Duration | Rationale |
|-----------|----------|----------------|-----------|
| User Profile | `/api/user/profile` | 24 hours | Rarely changes |
| Subscription Status | `/api/subscription` | 1 hour | Payment-related, needs freshness |
| Market Signals | `/api/signals/market` | 5 minutes | Core feature, updates frequently |
| Market Signals Grouped | `/api/signals/market/grouped` | 5 minutes | Same as above |
| Analysis History | `/api/analysis/history` | 15 minutes | User's own analyses |
| Saved Analyses | `/api/saved-analyses` | 15 minutes | User-generated content |

### Medium Priority (Market Data)

| Data Type | Endpoint | Cache Duration | Rationale |
|-----------|----------|----------------|-----------|
| Insider Trading | `/api/insider-trading` | 30 minutes | Updates periodically |
| Congress Trading | `/api/congress-trading` | 1 hour | Delayed disclosure |
| Options Flow | `/api/options-flow` | 15 minutes | Time-sensitive |
| Social Sentiment | `/api/social-sentiment` | 15 minutes | Changes frequently |
| Dark Pool Data | `/api/off-exchange` | 30 minutes | EOD data mostly |
| Government Contracts | `/api/government-contracts` | 6 hours | Slow updates |
| Lobbyist Activity | `/api/lobbyist-activity` | 6 hours | Periodic filings |
| Political Donations | `/api/political-donations` | 24 hours | Quarterly updates |

### Low Priority (On-Demand)

| Data Type | Endpoint | Cache Duration | Rationale |
|-----------|----------|----------------|-----------|
| Trade Ideas | `/api/analysis/trade-ideas` | 30 minutes | AI-generated |
| Subscription Pricing | `/api/subscriptions/pricing` | 24 hours | Rarely changes |

### Not Cached

| Data Type | Endpoint | Reason |
|-----------|----------|--------|
| Chart Analysis | `/api/chart-analysis` | User-initiated, unique each time |
| Chat Messages | `/api/chat` | Conversational, needs real-time |
| Checkout Session | `/api/create-checkout-session` | Security-sensitive |

---

## Technical Specifications

### 1. Database: SQLDelight

**Rationale:** SQLDelight is the recommended KMP database solution:
- Generates type-safe Kotlin APIs from SQL
- Works on Android (SQLite) and iOS (SQLite via native driver)
- Supports migrations
- Compile-time SQL verification

**Dependencies to add:**
```kotlin
// build.gradle.kts (commonMain)
implementation("app.cash.sqldelight:runtime:2.0.1")
implementation("app.cash.sqldelight:coroutines-extensions:2.0.1")

// androidMain
implementation("app.cash.sqldelight:android-driver:2.0.1")

// iosMain
implementation("app.cash.sqldelight:native-driver:2.0.1")
```

### 2. Database Schema

```sql
-- Cache metadata table
CREATE TABLE cache_metadata (
    cache_key TEXT NOT NULL PRIMARY KEY,
    cached_at INTEGER NOT NULL,
    expires_at INTEGER NOT NULL,
    etag TEXT
);

-- User profile cache
CREATE TABLE cached_user_profile (
    id TEXT NOT NULL PRIMARY KEY,
    email TEXT NOT NULL,
    full_name TEXT NOT NULL,
    joined_date TEXT NOT NULL,
    subscription_type TEXT NOT NULL,
    subscription_status TEXT NOT NULL,
    plan_type TEXT,
    next_billing_date TEXT,
    trial_ends_in INTEGER,
    language TEXT NOT NULL,
    notifications_enabled INTEGER NOT NULL,
    market_alerts_enabled INTEGER NOT NULL,
    theme TEXT NOT NULL,
    cached_at INTEGER NOT NULL
);

-- Market signals cache
CREATE TABLE cached_market_signal (
    id TEXT NOT NULL PRIMARY KEY,
    pair TEXT NOT NULL,
    signal_type TEXT NOT NULL,
    price_from TEXT NOT NULL,
    price_to TEXT NOT NULL,
    action TEXT NOT NULL,
    icon_letter TEXT NOT NULL,
    icon_color TEXT NOT NULL,
    action_text_color TEXT NOT NULL,
    action_bg_color TEXT NOT NULL,
    timestamp INTEGER NOT NULL,
    entry_price REAL,
    closing_price REAL,
    total_pl REAL,
    percent_return REAL,
    take_profit REAL,
    stop_loss REAL,
    confidence_score REAL,
    analyst_notes TEXT,
    generated_time TEXT,
    closed_time TEXT,
    status TEXT,
    date_group TEXT NOT NULL,
    cached_at INTEGER NOT NULL
);

-- Insider trading cache
CREATE TABLE cached_insider_trade (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    ticker TEXT NOT NULL,
    company TEXT NOT NULL,
    insider_name TEXT NOT NULL,
    insider_title TEXT NOT NULL,
    transaction_type TEXT NOT NULL,
    shares REAL NOT NULL,
    price_per_share REAL NOT NULL,
    total_value REAL NOT NULL,
    transaction_date TEXT NOT NULL,
    filing_date TEXT NOT NULL,
    form_type TEXT NOT NULL,
    sec_filing_url TEXT,
    performance REAL,
    cached_at INTEGER NOT NULL
);

-- Congress trading cache
CREATE TABLE cached_congress_trade (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    politician TEXT NOT NULL,
    party TEXT NOT NULL,
    state TEXT NOT NULL,
    position TEXT NOT NULL,
    ticker TEXT NOT NULL,
    company TEXT NOT NULL,
    transaction_type TEXT NOT NULL,
    amount TEXT NOT NULL,
    transaction_date TEXT NOT NULL,
    filing_date TEXT NOT NULL,
    disclosure_url TEXT,
    performance REAL,
    cached_at INTEGER NOT NULL
);

-- Options flow cache
CREATE TABLE cached_options_flow (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    ticker TEXT NOT NULL,
    trade_date TEXT NOT NULL,
    expiration_date TEXT NOT NULL,
    strike REAL NOT NULL,
    option_type TEXT NOT NULL,
    volume INTEGER NOT NULL,
    open_interest INTEGER NOT NULL,
    premium REAL NOT NULL,
    sentiment TEXT NOT NULL,
    is_unusual INTEGER NOT NULL,
    cached_at INTEGER NOT NULL
);

-- Social sentiment cache
CREATE TABLE cached_social_sentiment (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    ticker TEXT NOT NULL,
    company TEXT NOT NULL,
    platform TEXT,
    mentions_count INTEGER NOT NULL,
    sentiment_score REAL NOT NULL,
    sentiment_label TEXT NOT NULL,
    volume_change REAL NOT NULL,
    top_posts_json TEXT NOT NULL,
    timestamp TEXT,
    cached_at INTEGER NOT NULL
);

-- Dark pool data cache
CREATE TABLE cached_dark_pool (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    ticker TEXT NOT NULL,
    company TEXT NOT NULL,
    total_volume INTEGER NOT NULL,
    dark_pool_volume INTEGER NOT NULL,
    dark_pool_percent REAL NOT NULL,
    short_volume INTEGER NOT NULL,
    short_percent REAL NOT NULL,
    top_venues_json TEXT NOT NULL,
    date TEXT,
    cached_at INTEGER NOT NULL
);

-- Government contracts cache
CREATE TABLE cached_government_contract (
    contract_id TEXT NOT NULL PRIMARY KEY,
    company TEXT NOT NULL,
    ticker TEXT,
    agency TEXT NOT NULL,
    description TEXT NOT NULL,
    amount REAL NOT NULL,
    award_type TEXT NOT NULL,
    award_date TEXT NOT NULL,
    start_date TEXT,
    end_date TEXT,
    sector TEXT,
    status TEXT,
    naics_code TEXT,
    contract_url TEXT,
    cached_at INTEGER NOT NULL
);

-- Lobbyist activity cache
CREATE TABLE cached_lobbyist_activity (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    company TEXT NOT NULL,
    ticker TEXT,
    lobbyist_name TEXT NOT NULL,
    client_name TEXT NOT NULL,
    amount REAL NOT NULL,
    issue_areas_json TEXT,
    specific_issues TEXT,
    report_year INTEGER NOT NULL,
    filing_date TEXT NOT NULL,
    cached_at INTEGER NOT NULL
);

-- Political donations cache
CREATE TABLE cached_political_donation (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    company TEXT NOT NULL,
    ticker TEXT,
    donor_name TEXT NOT NULL,
    recipient_name TEXT NOT NULL,
    recipient_type TEXT NOT NULL,
    recipient_party TEXT NOT NULL,
    donation_type TEXT NOT NULL,
    amount REAL NOT NULL,
    election_year INTEGER NOT NULL,
    filing_date TEXT NOT NULL,
    cached_at INTEGER NOT NULL
);

-- Analysis history cache
CREATE TABLE cached_analysis (
    id TEXT NOT NULL PRIMARY KEY,
    order_id TEXT,
    asset TEXT,
    analysis_type TEXT NOT NULL,
    confidence_score REAL,
    chart_image_url TEXT,
    chart_image_base64 TEXT,
    chart_mime_type TEXT,
    key_insights_json TEXT,
    gameplan_json TEXT,
    additional_details_json TEXT,
    timestamp INTEGER,
    created_at TEXT,
    date_group TEXT NOT NULL,
    is_saved INTEGER NOT NULL DEFAULT 0,
    cached_at INTEGER NOT NULL
);

-- Subscription info cache
CREATE TABLE cached_subscription (
    id INTEGER NOT NULL PRIMARY KEY DEFAULT 1,
    has_subscription INTEGER NOT NULL,
    plan_id TEXT,
    plan_name TEXT,
    status TEXT,
    current_period_start TEXT,
    current_period_end TEXT,
    days_until_renewal INTEGER,
    upcoming_amount INTEGER,
    cached_at INTEGER NOT NULL
);

-- Trade ideas cache
CREATE TABLE cached_trade_idea (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    ticker TEXT NOT NULL,
    company TEXT NOT NULL,
    recommendation TEXT NOT NULL,
    confidence REAL NOT NULL,
    reasoning TEXT NOT NULL,
    price_target REAL,
    stop_loss REAL,
    timeframe TEXT NOT NULL,
    catalysts_json TEXT,
    risks_json TEXT,
    cached_at INTEGER NOT NULL
);

-- Subscription pricing cache
CREATE TABLE cached_subscription_pricing (
    plan_type TEXT NOT NULL PRIMARY KEY,
    price REAL NOT NULL,
    currency TEXT NOT NULL,
    billing_frequency TEXT NOT NULL,
    trial_days INTEGER,
    discount INTEGER,
    price_id TEXT NOT NULL,
    features_json TEXT NOT NULL,
    cached_at INTEGER NOT NULL
);

-- Indexes for performance
CREATE INDEX idx_insider_trade_ticker ON cached_insider_trade(ticker);
CREATE INDEX idx_congress_trade_ticker ON cached_congress_trade(ticker);
CREATE INDEX idx_options_flow_ticker ON cached_options_flow(ticker);
CREATE INDEX idx_market_signal_date ON cached_market_signal(date_group);
CREATE INDEX idx_analysis_date ON cached_analysis(date_group);
CREATE INDEX idx_cache_metadata_expires ON cache_metadata(expires_at);
```

### 3. Architecture Changes

#### New Package Structure
```
composeApp/src/commonMain/kotlin/com/myopenclaw/
├── data/
│   ├── local/
│   │   ├── cache/
│   │   │   ├── CacheDatabase.kt           # SQLDelight database
│   │   │   ├── CacheManager.kt            # Central cache orchestrator
│   │   │   ├── CachePolicy.kt             # Cache duration configs
│   │   │   └── mappers/                   # Domain <-> Cache mappers
│   │   │       ├── MarketSignalCacheMapper.kt
│   │   │       ├── InsiderTradeCacheMapper.kt
│   │   │       └── ...
│   │   └── preferences/                   # Existing preferences
│   ├── remote/                            # Existing API service
│   └── repository/
│       ├── MarketDataRepositoryImpl.kt    # Updated with caching
│       └── ...
├── domain/
│   ├── cache/
│   │   ├── CacheStrategy.kt               # Cache-first, network-first, etc.
│   │   └── CacheResult.kt                 # Sealed class for cache states
│   └── ...
└── ...
```

#### CacheResult Sealed Class
```kotlin
sealed class CacheResult<out T> {
    data class Fresh<T>(val data: T) : CacheResult<T>()
    data class Stale<T>(val data: T, val isRefreshing: Boolean) : CacheResult<T>()
    data class Loading<T>(val cachedData: T? = null) : CacheResult<T>()
    data class Error<T>(val error: Throwable, val cachedData: T? = null) : CacheResult<T>()
}
```

#### CachePolicy Configuration
```kotlin
object CachePolicy {
    // Durations in milliseconds
    const val USER_PROFILE = 24 * 60 * 60 * 1000L      // 24 hours
    const val SUBSCRIPTION = 60 * 60 * 1000L           // 1 hour
    const val MARKET_SIGNALS = 5 * 60 * 1000L          // 5 minutes
    const val ANALYSIS_HISTORY = 15 * 60 * 1000L       // 15 minutes
    const val INSIDER_TRADING = 30 * 60 * 1000L        // 30 minutes
    const val CONGRESS_TRADING = 60 * 60 * 1000L       // 1 hour
    const val OPTIONS_FLOW = 15 * 60 * 1000L           // 15 minutes
    const val SOCIAL_SENTIMENT = 15 * 60 * 1000L       // 15 minutes
    const val DARK_POOL = 30 * 60 * 1000L              // 30 minutes
    const val GOVERNMENT_CONTRACTS = 6 * 60 * 60 * 1000L  // 6 hours
    const val LOBBYIST_ACTIVITY = 6 * 60 * 60 * 1000L     // 6 hours
    const val POLITICAL_DONATIONS = 24 * 60 * 60 * 1000L  // 24 hours
    const val TRADE_IDEAS = 30 * 60 * 1000L            // 30 minutes
    const val SUBSCRIPTION_PRICING = 24 * 60 * 60 * 1000L // 24 hours
}
```

### 4. Repository Pattern Update

Example for `MarketDataRepositoryImpl`:

```kotlin
class MarketDataRepositoryImpl(
    private val apiService: ApiService,
    private val cacheManager: CacheManager
) : MarketDataRepository {

    override fun getInsiderTrading(
        limit: Int,
        ticker: String?,
        forceRefresh: Boolean
    ): Flow<CacheResult<List<InsiderTrade>>> = flow {
        val cacheKey = "insider_trading_${limit}_${ticker ?: "all"}"

        // 1. Emit cached data immediately if available
        val cachedData = cacheManager.getInsiderTrades(cacheKey)
        if (cachedData != null) {
            val isFresh = cacheManager.isCacheFresh(cacheKey, CachePolicy.INSIDER_TRADING)
            if (isFresh && !forceRefresh) {
                emit(CacheResult.Fresh(cachedData))
                return@flow
            }
            emit(CacheResult.Stale(cachedData, isRefreshing = true))
        } else {
            emit(CacheResult.Loading())
        }

        // 2. Fetch fresh data from API
        try {
            val response = apiService.getInsiderTrading(limit, ticker)
            if (response.success && response.data != null) {
                cacheManager.saveInsiderTrades(cacheKey, response.data)
                emit(CacheResult.Fresh(response.data))
            } else {
                emit(CacheResult.Error(
                    Exception(response.error ?: "Unknown error"),
                    cachedData
                ))
            }
        } catch (e: Exception) {
            emit(CacheResult.Error(e, cachedData))
        }
    }
}
```

### 5. ViewModel Update

Example for `InsiderTradingViewModel`:

```kotlin
class InsiderTradingViewModel(
    private val getInsiderTradingUseCase: GetInsiderTradingUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<InsiderTradingState>(InsiderTradingState.Loading())
    val state: StateFlow<InsiderTradingState> = _state

    fun loadTrades(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            getInsiderTradingUseCase(forceRefresh = forceRefresh)
                .collect { result ->
                    _state.value = when (result) {
                        is CacheResult.Fresh -> InsiderTradingState.Success(
                            trades = result.data,
                            isFromCache = false
                        )
                        is CacheResult.Stale -> InsiderTradingState.Success(
                            trades = result.data,
                            isFromCache = true,
                            isRefreshing = result.isRefreshing
                        )
                        is CacheResult.Loading -> InsiderTradingState.Loading(
                            cachedTrades = result.cachedData
                        )
                        is CacheResult.Error -> InsiderTradingState.Error(
                            message = result.error.message ?: "Unknown error",
                            cachedTrades = result.cachedData
                        )
                    }
                }
        }
    }

    fun refresh() = loadTrades(forceRefresh = true)
}

sealed class InsiderTradingState {
    data class Loading(val cachedTrades: List<InsiderTrade>? = null) : InsiderTradingState()
    data class Success(
        val trades: List<InsiderTrade>,
        val isFromCache: Boolean = false,
        val isRefreshing: Boolean = false
    ) : InsiderTradingState()
    data class Error(
        val message: String,
        val cachedTrades: List<InsiderTrade>? = null
    ) : InsiderTradingState()
}
```

### 6. Platform-Specific Database Drivers

```kotlin
// commonMain - expect declaration
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

// androidMain - actual implementation
actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = myOpenClawDatabase.Schema,
            context = context,
            name = "myopenclaw.db"
        )
    }
}

// iosMain - actual implementation
actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = myOpenClawDatabase.Schema,
            name = "myopenclaw.db"
        )
    }
}
```

---

## Implementation Tasks

### Phase 1: Infrastructure Setup (Sprint 1)

- [ ] **1.1** Add SQLDelight dependencies to `build.gradle.kts`
- [ ] **1.2** Configure SQLDelight plugin and database schema location
- [ ] **1.3** Create database schema file (`myOpenClaw.sq`)
- [ ] **1.4** Implement `DatabaseDriverFactory` for Android
- [ ] **1.5** Implement `DatabaseDriverFactory` for iOS
- [ ] **1.6** Create `CachePolicy` configuration object
- [ ] **1.7** Create `CacheResult` sealed class
- [ ] **1.8** Create `CacheManager` interface and implementation
- [ ] **1.9** Add cache module to Koin DI configuration
- [ ] **1.10** Write unit tests for cache infrastructure

### Phase 2: Core Data Caching (Sprint 2)

- [ ] **2.1** Implement User Profile caching
  - [ ] Create cache mapper
  - [ ] Update `UserRepository`
  - [ ] Update `ProfileViewModel`
- [ ] **2.2** Implement Market Signals caching
  - [ ] Create cache mapper
  - [ ] Update `SignalsRepository`
  - [ ] Update `SignalsViewModel`
- [ ] **2.3** Implement Analysis History caching
  - [ ] Create cache mapper
  - [ ] Update `AnalysisRepository`
  - [ ] Update `AnalysisHistoryViewModel`
- [ ] **2.4** Implement Subscription caching
  - [ ] Create cache mapper
  - [ ] Update `SubscriptionRepository`
  - [ ] Update related ViewModels

### Phase 3: Market Data Caching (Sprint 3)

- [ ] **3.1** Implement Insider Trading caching
- [ ] **3.2** Implement Congress Trading caching
- [ ] **3.3** Implement Options Flow caching
- [ ] **3.4** Implement Social Sentiment caching
- [ ] **3.5** Implement Dark Pool caching
- [ ] **3.6** Implement Government Contracts caching
- [ ] **3.7** Implement Lobbyist Activity caching
- [ ] **3.8** Implement Political Donations caching
- [ ] **3.9** Implement Trade Ideas caching
- [ ] **3.10** Implement Subscription Pricing caching

### Phase 4: UI Integration (Sprint 4)

- [ ] **4.1** Update all ViewModels to use `CacheResult` states
- [ ] **4.2** Create `CachedDataIndicator` composable (shows "updating..." badge)
- [ ] **4.3** Add pull-to-refresh to all list screens
- [ ] **4.4** Create offline mode banner component
- [ ] **4.5** Update loading states to show cached data
- [ ] **4.6** Add error states with retry + cached data fallback
- [ ] **4.7** Add cache settings screen (view size, clear cache button)

### Phase 5: Polish & Optimization (Sprint 5)

- [ ] **5.1** Implement cache cleanup on logout
- [ ] **5.2** Add cache size monitoring and automatic cleanup
- [ ] **5.3** Implement cache migration strategy for schema updates
- [ ] **5.4** Add analytics for cache hit/miss rates
- [ ] **5.5** Performance testing and optimization
- [ ] **5.6** Write integration tests
- [ ] **5.7** Update documentation

---

## UI/UX Considerations

### Loading States

```
┌─────────────────────────────────────┐
│  📊 Insider Trading                 │
│  ─────────────────────────────────  │
│  ┌───────────────────────────────┐  │
│  │ AAPL  Tim Cook sold 50K      │  │  ← Cached data shown
│  │ $195.50  -2.3%               │  │    immediately
│  └───────────────────────────────┘  │
│  ┌───────────────────────────────┐  │
│  │ GOOGL  Sundar Pichai...      │  │
│  │ $142.30  +1.2%               │  │
│  └───────────────────────────────┘  │
│                                     │
│  ╔═══════════════════════════════╗  │
│  ║  🔄 Updating...               ║  │  ← Subtle refresh indicator
│  ╚═══════════════════════════════╝  │
└─────────────────────────────────────┘
```

### Offline Mode

```
┌─────────────────────────────────────┐
│  ⚠️ You're offline                  │  ← Persistent banner
│  Showing cached data from 2h ago    │
├─────────────────────────────────────┤
│  📊 Insider Trading                 │
│  ─────────────────────────────────  │
│  [cached data displays normally]    │
└─────────────────────────────────────┘
```

### Error with Cached Fallback

```
┌─────────────────────────────────────┐
│  📊 Insider Trading                 │
│  ─────────────────────────────────  │
│  [cached data]                      │
│                                     │
│  ╔═══════════════════════════════╗  │
│  ║  ⚠️ Couldn't refresh          ║  │
│  ║  [Retry]                      ║  │
│  ╚═══════════════════════════════╝  │
└─────────────────────────────────────┘
```

---

## Testing Strategy

### Unit Tests
- Cache read/write operations
- Cache expiration logic
- Mapper functions (domain ↔ cache)
- CacheManager business logic

### Integration Tests
- Repository caching flow
- Database migrations
- Platform-specific driver behavior

### UI Tests
- Cached data displays on screen open
- Pull-to-refresh triggers API call
- Offline mode banner appears correctly
- Error states show cached data fallback

---

## Success Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Time to first content | < 100ms | Analytics |
| Cache hit rate | > 80% | Analytics |
| App size increase | < 2MB | Build output |
| Database size (typical use) | < 20MB | Runtime measurement |
| User satisfaction (loading) | Improved | User feedback |

---

## Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| Stale data shown to users | Medium | Clear "updating" indicators, short cache durations for volatile data |
| Database corruption | High | Implement recovery mechanism, database versioning |
| Excessive storage usage | Medium | Automatic cleanup, size limits, LRU eviction |
| Cache invalidation complexity | Medium | Simple time-based expiration, force refresh option |
| Schema migration failures | High | Thorough migration testing, rollback capability |

---

## Dependencies

- SQLDelight 2.0.1+
- Kotlinx Coroutines (existing)
- Kotlinx Serialization (existing)
- Koin DI (existing)

---

## Open Questions

1. Should we cache images (chart screenshots) or just URLs?
2. Do we need ETag/If-Modified-Since support for bandwidth optimization?
3. Should cache be encrypted for sensitive financial data?
4. What's the max acceptable cache size before cleanup?

---

## References

- [SQLDelight Documentation](https://cashapp.github.io/sqldelight/)
- [Kotlin Multiplatform Mobile](https://kotlinlang.org/docs/multiplatform-mobile-getting-started.html)
- [Stale-While-Revalidate Pattern](https://web.dev/stale-while-revalidate/)
