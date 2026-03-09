# US-004: Watchlist Stock Management

## User Story
**As a** trading app user  
**I want to** add, remove, and manage stocks in my watchlist  
**So that** I can track insider trading, congressional activity, and options flow for my favorite stocks

## Acceptance Criteria

### 1. Empty State Display
- [x] When watchlist is empty, show empty state with bookmark icon
- [x] Display message: "No Stocks in Watchlist"
- [x] Show subtitle: "Add stocks to track insider trading, congressional activity, and more"
- [x] Display "Add Your First Stock" button
- [x] Show floating action button (+) for adding stocks

### 2. Add Stock Functionality
- [x] Tap "Add Your First Stock" button or FAB opens stock search screen
- [x] Stock search screen has search bar at top
- [x] As user types, display matching stock results
- [x] Each result shows: ticker symbol, company name, current price, change percentage
- [x] Tap on a stock result to add it to watchlist
- [x] Show success message when stock is added
- [x] Prevent duplicate stocks from being added
- [x] Navigate back to watchlist after adding stock

### 3. Display Watchlist Items
- [x] Show list of watchlist items when not empty
- [x] Each item displays:
  - Ticker symbol with first letter in circular badge
  - Company name
  - Current price
  - Change percentage (green for positive, red for negative)
  - Activity badges (Insider, Congress, Options) when applicable
  - Alert count badge when > 0
- [x] Show portfolio summary card at top with:
  - Total Alerts count
  - Gainers count (positive change)
  - Losers count (negative change)

### 4. Remove Stock Functionality
- [x] Swipe left on stock item to reveal delete action
- [x] Show red background with delete icon during swipe
- [x] Show confirmation dialog: "Remove {TICKER}?"
- [x] Confirm dialog has "Remove" (red) and "Cancel" buttons
- [x] Remove stock from watchlist on confirmation
- [x] Show success message after removal
- [x] Update list immediately after removal

### 5. Search and Sort
- [x] Search icon in top bar opens search interface
- [x] Search bar allows filtering by ticker or company name
- [x] Sort icon in top bar shows sort options:
  - Name (A-Z / Z-A)
  - Price (Low-High / High-Low)
  - Change % (Low-High / High-Low)
  - Alerts (Low-High / High-Low)
- [x] Tap sort option toggles ascending/descending
- [x] Active sort option shows arrow indicator

### 6. Navigation
- [x] Tap on watchlist item navigates to stock details
- [x] Stock details screen shows comprehensive data (future enhancement - currently redirects to chart analysis)

### 7. Data Persistence
- [x] Watchlist data persists locally using PreferencesManager
- [x] Watchlist loads on app restart
- [x] Handle loading states (show spinner)
- [x] Handle error states (show error message with retry button)

### 8. Mock Data for Testing
- [x] Pre-populate with sample stocks for testing:
  - AAPL (Apple Inc.)
  - TSLA (Tesla, Inc.)
  - MSFT (Microsoft Corporation)
  - NVDA (NVIDIA Corporation)
  - META (Meta Platforms, Inc.)
  - Plus 10 more stocks (GOOGL, AMZN, AMD, NFLX, DIS, BA, JPM, V, WMT, COIN)

## Technical Implementation

### Files to Create/Modify

#### New Files
1. `AddStockScreen.kt` - Stock search and add screen
2. `StockSearchViewModel.kt` - ViewModel for stock search
3. `StockSearchRepository.kt` - Repository for fetching stock data
4. `StockSearchRepositoryImpl.kt` - Implementation with mock data

#### Modified Files
1. `NavGraph.kt` - Add AddStock route and update Watchlist screen
2. `AppModule.kt` - Register new ViewModel and Repository in DI

### Data Models
```kotlin
data class StockSearchResult(
    val ticker: String,
    val companyName: String,
    val currentPrice: Double,
    val changePercent: Double,
    val volume: String
)
```

### Navigation Flow
```
Watchlist (Empty) 
  → Tap "Add Your First Stock" or FAB
  → AddStockScreen
  → Search & Select Stock
  → Add to Watchlist
  → Return to Watchlist (Populated)

Watchlist (Populated)
  → Tap FAB
  → AddStockScreen
  → Add more stocks
```

### Repository Methods
```kotlin
interface StockSearchRepository {
    suspend fun searchStocks(query: String): Result<List<StockSearchResult>>
    suspend fun getPopularStocks(): Result<List<StockSearchResult>>
}
```

## Testing Checklist

### Manual Testing
- [ ] Open app to watchlist tab (should show empty state)
- [ ] Tap "Add Your First Stock" button (should open search screen)
- [ ] Type "AAPL" in search (should show Apple Inc.)
- [ ] Tap on Apple result (should add to watchlist and navigate back)
- [ ] Verify Apple appears in watchlist
- [ ] Tap FAB to add another stock
- [ ] Add TSLA (should appear in list)
- [ ] Try adding AAPL again (should show error "Already in watchlist")
- [ ] Swipe left on AAPL (should show delete)
- [ ] Tap delete and confirm (should remove from list)
- [ ] Test search functionality (filter by ticker/name)
- [ ] Test all sort options
- [ ] Close and reopen app (watchlist should persist)
- [ ] Test with no network/offline mode

### Edge Cases
- [ ] Empty search query (show popular stocks)
- [ ] No search results found
- [ ] Duplicate stock addition attempt
- [ ] Removing last item from watchlist (return to empty state)
- [ ] Very long company names (proper text truncation)
- [ ] Special characters in search query

## Design Reference
Based on the provided screenshot showing:
- Dark theme with turquoise/teal accent color (#00D9C0)
- Empty state with circular icon background
- Bottom navigation: Home, Signals, AI Chat, Watchlist (active), Profile
- Material 3 design components

## Dependencies
- Existing: WatchlistViewModel, WatchlistRepository (already implemented ✓)
- Existing: PreferencesManager with watchlist methods (already implemented ✓)
- New: StockSearchViewModel, StockSearchRepository (to be implemented)

## Priority
**HIGH** - Core functionality for user engagement

## Estimated Effort
4-6 hours
- Add Stock Screen: 2 hours
- ViewModel & Repository: 1 hour  
- Navigation Integration: 1 hour
- Testing & Polish: 1-2 hours

## Status
✅ **COMPLETED** - January 28, 2026

All core functionality implemented and working:
- Stock search with debouncing
- Add/Remove stocks
- Data persistence
- Sort and filter
- Empty/Loading/Error states
- Mock data with 15 stocks

## Notes
- Start with mock data for stock search
- Later integrate with real stock API (Alpha Vantage, IEX Cloud, or backend API)
- Consider rate limiting for API calls
- Add debouncing to search input (300ms delay)
- Cache popular stocks for offline support
