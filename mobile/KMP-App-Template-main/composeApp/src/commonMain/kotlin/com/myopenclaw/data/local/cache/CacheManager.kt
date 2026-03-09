package com.myopenclaw.data.local.cache

import com.myopenclaw.domain.models.*

/**
 * CacheManager interface for managing local data cache.
 */
interface CacheManager {
    // ==================== Cache Metadata Operations ====================

    suspend fun isCacheFresh(cacheKey: String): Boolean
    suspend fun getCacheAge(cacheKey: String): Long?
    suspend fun updateCacheMetadata(cacheKey: String, durationMs: Long)

    // ==================== User Profile ====================

    suspend fun getUserProfile(): UserProfileData?
    suspend fun saveUserProfile(profile: UserProfileData)
    suspend fun clearUserProfile()

    // ==================== Subscription Pricing ====================

    suspend fun getSubscriptionPricing(): List<SubscriptionPricingData>
    suspend fun saveSubscriptionPricing(pricing: List<SubscriptionPricingData>)
    suspend fun clearSubscriptionPricing()

    // ==================== Global Cache Operations ====================

    suspend fun clearAllCache()
    suspend fun clearExpiredCache()
}
