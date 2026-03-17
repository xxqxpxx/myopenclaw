# Home Screen Implementation

## Overview
Pixel-perfect implementation of the my openClaw home/dashboard screen matching Figma designs exactly.

## Files
- `HomeScreenNew.kt` - Main home screen implementation with both states
- `HomeScreenPreview.kt` - Preview/demo screens for testing

## Features

### Two States
The screen supports two states controlled by the `hasTradeAnalysis` parameter:

1. **Empty State** (hasTradeAnalysis = false)
   - Shows when user has no trade analysis done
   - Displays empty state message with scan icon
   - Animated arrow pointing down
   - Call-to-action to start analyzing trades

2. **With Trade Analysis** (hasTradeAnalysis = true)
   - Shows when user has analyzed trades
   - Displays trade analysis section with 5 sample analyses
   - Each analysis card shows confidence level and action (BUY/SELL/HOLD)

### Components

#### 1. HomeHeader
- App icon (40dp x 40dp, rounded 12dp, Green2 background)
- App name "my openClaw" (16sp SemiBold)
- Pro badge (dark background, white text, 10sp)
- Currency/Language selector (56dp height, flag icon, dropdown)

#### 2. ActionCardsSection
- Two equal-width cards with 8dp gap
- "Analyse Trade chart" card (chart icon)
- "Ask questions" card (question icon)
- Each card: 88dp height, 16dp rounded corners
- Dark background (#2C3544)

#### 3. MarketSignalsSection
- Section header with "View All" link
- Sample signals (XRP, GBP/USD)
- Signal cards: 72dp height, circular icon (48dp), price range, action badge
- Icons with gradient/solid colors (purple, orange)

#### 4. TradeAnalysisSection (Conditional)
- Only visible when hasTradeAnalysis = true
- Section header with "View All" link
- 5 sample analysis cards
- Each card: 72dp height, circular icon (48dp), confidence %, action badge
- Icons with various colors (purple, yellow, green)

#### 5. EmptyStateSection (Conditional)
- Only visible when hasTradeAnalysis = false
- Centered content with scan icon
- Title: "No trade analysis done"
- Description text (14sp, 22sp line height)
- Animated rotating arrow (2s rotation)

#### 6. BottomNavigationBar
- Fixed at bottom, 80dp height
- 5 items: Home, Signals, Scan (center), Chat, Profile
- Center scan button: elevated 8dp, 56dp circular, Green2 background
- Active state: white icon/text
- Inactive state: 50% opacity white

## Design Specifications

### Colors
```kotlin
Dark1 = #0D1023           // Main background
InputBackground = #2C3544  // Card backgrounds
Green2 = #30CD8F          // Primary color, buttons
BullishGreen = #30CD8F    // BUY badges
BearishRed = #FF5252      // SELL badges
Warning = #FFB020         // HOLD badges
White = #FFFFFF           // Text and icons
```

### Typography
- App name: 16sp SemiBold
- Pro badge: 10sp SemiBold
- Action card label: 14sp Medium
- Section headers: 18sp Bold
- "View All": 14sp SemiBold
- Signal pair: 16sp Bold
- Signal type: 12sp Regular (70% opacity)
- Price range: 14sp SemiBold
- Action badge: 10sp Bold
- Confidence: 12sp Regular (70% opacity)
- Empty state title: 20sp Bold
- Empty state description: 14sp Regular (70% opacity, 22sp line height)
- Bottom nav label: 10sp Medium

### Spacing
- Screen padding: 16dp horizontal
- Card spacing: 12dp vertical
- Action cards gap: 8dp
- Section top margin: 24dp
- Icon-text gap: 8dp, 12dp
- Bottom nav padding: 16dp horizontal

### Sizes
- Header app icon: 40dp
- Action card height: 88dp
- Signal/Analysis card height: 72dp
- Icon circles: 48dp
- Action icons: 24dp
- Badge rounded corners: 10dp
- Card rounded corners: 16dp
- Empty state icon: 56dp
- Scan button: 56dp
- Bottom nav height: 80dp

## Usage

### Basic Usage
```kotlin
HomeScreenNew(
    hasTradeAnalysis = false, // or true
    onAnalyseTradeClick = { /* Navigate to analysis */ },
    onAskQuestionsClick = { /* Navigate to chat */ },
    onScanClick = { /* Navigate to scan */ }
)
```

### With All Callbacks
```kotlin
HomeScreenNew(
    hasTradeAnalysis = true,
    onAnalyseTradeClick = { navController.navigate("analyse") },
    onAskQuestionsClick = { navController.navigate("chat") },
    onSignalClick = { signal -> navController.navigate("signal/${signal.pair}") },
    onAnalysisClick = { analysis -> navController.navigate("analysis/${analysis.pair}") },
    onViewAllSignalsClick = { navController.navigate("signals") },
    onViewAllAnalysisClick = { navController.navigate("analyses") },
    onNavigateToSignals = { navController.navigate("signals") },
    onNavigateToChat = { navController.navigate("chat") },
    onNavigateToProfile = { navController.navigate("profile") },
    onScanClick = { navController.navigate("scan") }
)
```

### Preview/Testing
```kotlin
// Use the preview screen to toggle between states
HomeScreenPreview()

// Or use specific state demos
HomeScreenEmptyStateDemo()     // Empty state only
HomeScreenWithDataDemo()        // With data only
```

## Data Models

### MarketSignal
```kotlin
data class MarketSignal(
    val pair: String,        // e.g., "XRP", "GBP/USD"
    val type: String,        // e.g., "Crypto", "Currency"
    val priceFrom: String,   // e.g., "$26"
    val priceTo: String,     // e.g., "$265"
    val action: SignalAction,// BUY, SELL, HOLD
    val iconLetter: String,  // e.g., "X", "G"
    val iconColor: Color     // Circle background color
)
```

### TradeAnalysis
```kotlin
data class TradeAnalysis(
    val pair: String,        // e.g., "ETH/USD"
    val type: String,        // e.g., "Crypto"
    val confidence: Int,     // 0-100
    val action: SignalAction,// BUY, SELL, HOLD
    val iconLetter: String,  // e.g., "N", "Y"
    val iconColor: Color     // Circle background color
)
```

### SignalAction
```kotlin
enum class SignalAction {
    BUY,    // Green badge
    SELL,   // Red badge
    HOLD    // Orange/Warning badge
}
```

## Sample Data
The screen currently uses hardcoded sample data from:
- `getSampleMarketSignals()` - 2 signals (XRP, GBP/USD)
- `getSampleTradeAnalyses()` - 5 analyses (ETH, LTC, XRP, ADA, SOL)

To connect to real data, replace these functions with actual API calls or database queries.

## Animations
- Empty state arrow: 360° rotation in 2 seconds (continuous loop)
- Future enhancement: Add ripple effects on card clicks
- Future enhancement: Add shimmer loading states

## Navigation
All navigation is handled via callbacks. The parent composable should provide navigation logic:
- Analysis/Questions screens
- Signal/Analysis detail screens
- "View All" list screens
- Bottom navigation targets
- Scan screen

## Customization

### Changing Colors
Update the theme colors in `com.myopenclaw.ui.theme.Color.kt`:
```kotlin
val Dark1 = Color(0xFF0D1023)
val Green2 = Color(0xFF30CD8F)
// etc.
```

### Modifying Sample Data
Edit the `getSampleMarketSignals()` and `getSampleTradeAnalyses()` functions to change:
- Number of items
- Icon letters and colors
- Pair names and types
- Confidence levels
- Actions (BUY/SELL/HOLD)

### Adjusting Spacing
All spacing is hardcoded with dp values for pixel-perfect design. To adjust:
- Change padding values in component modifiers
- Update card heights (88.dp, 72.dp)
- Modify icon sizes (24.dp, 48.dp)
- Adjust gaps in Row/Column arrangements

## Future Enhancements
1. Connect to real API/database for signals and analyses
2. Add pull-to-refresh functionality
3. Implement search/filter for signals and analyses
4. Add skeleton/shimmer loading states
5. Implement pagination for long lists
6. Add swipe gestures for quick actions
7. Implement real-time updates for signals
8. Add notification badges for new signals
9. Implement language selector functionality
10. Add accessibility labels and support

## Testing
Use `HomeScreenPreview.kt` to test both states:
1. Run the app and navigate to preview screen
2. Toggle the switch to see empty vs. with-data states
3. Click on cards to verify callbacks work
4. Test bottom navigation
5. Verify all text, colors, spacing match Figma designs

## Notes
- Bottom navigation overlays the content (LazyColumn has 96.dp bottom padding)
- Scan button is elevated 8dp above the nav bar
- All colors, spacing, typography match Figma designs exactly
- Screen is fully scrollable when content exceeds viewport
- Both states maintain the same header and action cards
- Empty state replaces the trade analysis section
