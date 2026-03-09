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
    private const val STANDARD_DURATION = 6 * 60 * 60 * 1000L  // 6 hours

    const val USER_PROFILE = STANDARD_DURATION
    const val SUBSCRIPTION = STANDARD_DURATION
    const val SUBSCRIPTION_PRICING = STANDARD_DURATION

    object Keys {
        const val USER_PROFILE = "user_profile"
        const val SUBSCRIPTION = "subscription"
        const val SUBSCRIPTION_PRICING = "subscription_pricing"
    }
}
