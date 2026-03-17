package com.myopenclaw.data.local.cache

import com.myopenclaw.domain.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Implementation of CacheManager using SQLDelight database.
 */
class CacheManagerImpl(
    private val database: MyOpenClawDatabase
) : CacheManager {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val queries get() = database.myOpenClawDatabaseQueries

    // ==================== Cache Metadata Operations ====================

    override suspend fun isCacheFresh(cacheKey: String): Boolean = withContext(Dispatchers.IO) {
        val metadata = queries.getCacheMetadata(cacheKey).executeAsOneOrNull()
        if (metadata == null) return@withContext false

        val currentTime = Clock.System.now().toEpochMilliseconds()
        metadata.expires_at > currentTime
    }

    override suspend fun getCacheAge(cacheKey: String): Long? = withContext(Dispatchers.IO) {
        val metadata = queries.getCacheMetadata(cacheKey).executeAsOneOrNull()
            ?: return@withContext null

        Clock.System.now().toEpochMilliseconds() - metadata.cached_at
    }

    override suspend fun updateCacheMetadata(cacheKey: String, durationMs: Long) = withContext(Dispatchers.IO) {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        queries.insertOrReplaceCacheMetadata(
            cache_key = cacheKey,
            cached_at = currentTime,
            expires_at = currentTime + durationMs
        )
    }

    // ==================== User Profile ====================

    override suspend fun getUserProfile(): UserProfileData? = withContext(Dispatchers.IO) {
        val cached = queries.getUserProfile().executeAsOneOrNull() ?: return@withContext null

        UserProfileData(
            id = cached.id,
            email = cached.email,
            fullName = cached.full_name,
            joinedDate = cached.joined_date,
            subscription = SubscriptionInfoData(
                type = cached.subscription_type,
                status = cached.subscription_status,
                planType = cached.plan_type,
                billingCycle = cached.billing_cycle,
                nextBillingDate = cached.next_billing_date,
                trialEndsIn = cached.trial_ends_in?.toInt(),
                renewalFrequency = cached.renewal_frequency
            ),
            preferences = UserPreferencesData(
                language = cached.language,
                notificationsEnabled = cached.notifications_enabled == 1L,
                marketAlertsEnabled = cached.market_alerts_enabled == 1L,
                theme = cached.theme
            )
        )
    }

    override suspend fun saveUserProfile(profile: UserProfileData) = withContext(Dispatchers.IO) {
        queries.insertOrReplaceUserProfile(
            id = profile.id,
            email = profile.email,
            full_name = profile.fullName,
            joined_date = profile.joinedDate,
            subscription_type = profile.subscription.type,
            subscription_status = profile.subscription.status,
            plan_type = profile.subscription.planType,
            billing_cycle = profile.subscription.billingCycle,
            next_billing_date = profile.subscription.nextBillingDate,
            trial_ends_in = profile.subscription.trialEndsIn?.toLong(),
            renewal_frequency = profile.subscription.renewalFrequency,
            language = profile.preferences.language,
            notifications_enabled = if (profile.preferences.notificationsEnabled) 1L else 0L,
            market_alerts_enabled = if (profile.preferences.marketAlertsEnabled) 1L else 0L,
            theme = profile.preferences.theme,
            cached_at = Clock.System.now().toEpochMilliseconds()
        )
        updateCacheMetadata(CachePolicy.Keys.USER_PROFILE, CachePolicy.USER_PROFILE)
    }

    override suspend fun clearUserProfile() = withContext(Dispatchers.IO) {
        queries.deleteUserProfile()
        queries.deleteCacheMetadata(CachePolicy.Keys.USER_PROFILE)
    }

    // ==================== Subscription Pricing ====================

    override suspend fun getSubscriptionPricing(): List<SubscriptionPricingData> = withContext(Dispatchers.IO) {
        queries.getAllSubscriptionPricing().executeAsList().map { cached ->
            val features: List<PremiumFeatureData> = try {
                json.decodeFromString(cached.features_json)
            } catch (e: Exception) {
                emptyList()
            }

            SubscriptionPricingData(
                planType = cached.plan_type,
                price = cached.price,
                currency = cached.currency,
                billingFrequency = cached.billing_frequency,
                trialDays = cached.trial_days?.toInt(),
                discount = cached.discount?.toInt(),
                originalPrice = cached.original_price,
                priceId = cached.price_id,
                features = features
            )
        }
    }

    override suspend fun saveSubscriptionPricing(pricing: List<SubscriptionPricingData>) = withContext(Dispatchers.IO) {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        queries.deleteAllSubscriptionPricing()

        pricing.forEach { plan ->
            queries.insertOrReplaceSubscriptionPricing(
                plan_type = plan.planType,
                price = plan.price,
                currency = plan.currency,
                billing_frequency = plan.billingFrequency,
                trial_days = plan.trialDays?.toLong(),
                discount = plan.discount?.toLong(),
                original_price = plan.originalPrice,
                price_id = plan.priceId,
                features_json = json.encodeToString(plan.features),
                cached_at = currentTime
            )
        }
        updateCacheMetadata(CachePolicy.Keys.SUBSCRIPTION_PRICING, CachePolicy.SUBSCRIPTION_PRICING)
    }

    override suspend fun clearSubscriptionPricing() = withContext(Dispatchers.IO) {
        queries.deleteAllSubscriptionPricing()
        queries.deleteCacheMetadata(CachePolicy.Keys.SUBSCRIPTION_PRICING)
    }

    // ==================== Global Cache Operations ====================

    override suspend fun clearAllCache() = withContext(Dispatchers.IO) {
        queries.clearAllCacheMetadata()
        queries.deleteUserProfile()
        queries.deleteAllSubscriptionPricing()
    }

    override suspend fun clearExpiredCache() = withContext(Dispatchers.IO) {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        queries.deleteExpiredCache(currentTime)
    }
}
