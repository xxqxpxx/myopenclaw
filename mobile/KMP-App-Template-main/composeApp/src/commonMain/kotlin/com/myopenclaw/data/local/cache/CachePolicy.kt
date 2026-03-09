package com.myopenclaw.data.local.cache

/**
 * Cache duration policies for different data types.
 * Each duration is specified in milliseconds.
 *
 * All data types use a 6-hour cache duration for consistency.
 * The app uses a stale-while-revalidate pattern, so cached data
 * is shown immediately while fresh data is fetched in the background.
 */
object CachePolicy {
    // Standard cache duration for all data types (6 hours)
    private const val STANDARD_DURATION = 6 * 60 * 60 * 1000L  // 6 hours

    // User-related data
    const val USER_PROFILE = STANDARD_DURATION               // 6 hours

    // Subscription data
    const val SUBSCRIPTION = STANDARD_DURATION               // 6 hours
    const val SUBSCRIPTION_PRICING = STANDARD_DURATION       // 6 hours

    // Core trading signals
    const val MARKET_SIGNALS = STANDARD_DURATION             // 6 hours

    // Analysis history
    const val ANALYSIS_HISTORY = STANDARD_DURATION           // 6 hours
    const val SAVED_ANALYSES = STANDARD_DURATION             // 6 hours

    // Market data - options and sentiment
    const val OPTIONS_FLOW = STANDARD_DURATION               // 6 hours
    const val SOCIAL_SENTIMENT = STANDARD_DURATION           // 6 hours

    // Market data - trading activity
    const val INSIDER_TRADING = STANDARD_DURATION            // 6 hours
    const val DARK_POOL = STANDARD_DURATION                  // 6 hours
    const val TRADE_IDEAS = STANDARD_DURATION                // 6 hours

    // Market data - congress trading
    const val CONGRESS_TRADING = STANDARD_DURATION           // 6 hours

    // Government/political data
    const val GOVERNMENT_CONTRACTS = STANDARD_DURATION       // 6 hours
    const val LOBBYIST_ACTIVITY = STANDARD_DURATION          // 6 hours
    const val POLITICAL_DONATIONS = STANDARD_DURATION        // 6 hours

    /**
     * Cache keys for different data types.
     * These are used to track cache metadata.
     */
    object Keys {
        const val USER_PROFILE = "user_profile"
        const val SUBSCRIPTION = "subscription"
        const val SUBSCRIPTION_PRICING = "subscription_pricing"
        const val MARKET_SIGNALS = "market_signals"
        const val MARKET_SIGNALS_GROUPED = "market_signals_grouped"
        const val ANALYSIS_HISTORY = "analysis_history"
        const val ANALYSIS_HISTORY_GROUPED = "analysis_history_grouped"
        const val SAVED_ANALYSES = "saved_analyses"
        const val INSIDER_TRADING = "insider_trading"
        const val CONGRESS_TRADING = "congress_trading"
        const val OPTIONS_FLOW = "options_flow"
        const val SOCIAL_SENTIMENT = "social_sentiment"
        const val DARK_POOL = "dark_pool"
        const val GOVERNMENT_CONTRACTS = "government_contracts"
        const val LOBBYIST_ACTIVITY = "lobbyist_activity"
        const val POLITICAL_DONATIONS = "political_donations"
        const val TRADE_IDEAS = "trade_ideas"

        /**
         * Generate a cache key with optional parameters.
         * Example: insider_trading_50_AAPL
         */
        fun withParams(baseKey: String, vararg params: Any?): String {
            val nonNullParams = params.filterNotNull()
            return if (nonNullParams.isEmpty()) {
                baseKey
            } else {
                "${baseKey}_${nonNullParams.joinToString("_")}"
            }
        }
    }
}
