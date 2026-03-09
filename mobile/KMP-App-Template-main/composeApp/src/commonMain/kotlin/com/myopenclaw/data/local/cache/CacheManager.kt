package com.myopenclaw.data.local.cache

import com.myopenclaw.domain.models.*
import kotlinx.coroutines.flow.Flow

/**
 * CacheManager interface for managing local data cache.
 *
 * Provides methods to:
 * - Check cache freshness
 * - Store and retrieve cached data
 * - Clear cache on logout or manually
 */
interface CacheManager {
    // ==================== Cache Metadata Operations ====================

    /**
     * Check if cache for the given key is fresh (not expired).
     */
    suspend fun isCacheFresh(cacheKey: String): Boolean

    /**
     * Get the age of cache in milliseconds.
     * Returns null if cache doesn't exist.
     */
    suspend fun getCacheAge(cacheKey: String): Long?

    /**
     * Update cache metadata for the given key.
     */
    suspend fun updateCacheMetadata(cacheKey: String, durationMs: Long)

    // ==================== User Profile ====================

    suspend fun getUserProfile(): UserProfileData?
    suspend fun saveUserProfile(profile: UserProfileData)
    suspend fun clearUserProfile()

    // ==================== Market Signals ====================

    suspend fun getMarketSignals(): List<MarketSignalData>
    suspend fun saveMarketSignals(signals: List<MarketSignalData>, dateGroup: String = "all")
    suspend fun getMarketSignalsGrouped(): List<SignalGroupData>
    suspend fun saveMarketSignalsGrouped(groups: List<SignalGroupData>)
    suspend fun clearMarketSignals()

    // ==================== Insider Trading ====================

    suspend fun getInsiderTrades(): List<InsiderTrade>
    suspend fun saveInsiderTrades(trades: List<InsiderTrade>)
    suspend fun clearInsiderTrades()

    // ==================== Congress Trading ====================

    suspend fun getCongressTrades(): List<CongressTrade>
    suspend fun saveCongressTrades(trades: List<CongressTrade>)
    suspend fun clearCongressTrades()

    // ==================== Options Flow ====================

    suspend fun getOptionsFlow(): List<OptionsFlow>
    suspend fun saveOptionsFlow(options: List<OptionsFlow>)
    suspend fun clearOptionsFlow()

    // ==================== Social Sentiment ====================

    suspend fun getSocialSentiment(): List<SocialSentiment>
    suspend fun saveSocialSentiment(sentiments: List<SocialSentiment>)
    suspend fun clearSocialSentiment()

    // ==================== Dark Pool ====================

    suspend fun getDarkPoolData(): List<DarkPoolData>
    suspend fun saveDarkPoolData(data: List<DarkPoolData>)
    suspend fun clearDarkPoolData()

    // ==================== Government Contracts ====================

    suspend fun getGovernmentContracts(): List<GovernmentContract>
    suspend fun saveGovernmentContracts(contracts: List<GovernmentContract>)
    suspend fun clearGovernmentContracts()

    // ==================== Lobbyist Activity ====================

    suspend fun getLobbyistActivity(): List<LobbyistActivity>
    suspend fun saveLobbyistActivity(activities: List<LobbyistActivity>)
    suspend fun clearLobbyistActivity()

    // ==================== Political Donations ====================

    suspend fun getPoliticalDonations(): List<PoliticalDonation>
    suspend fun savePoliticalDonations(donations: List<PoliticalDonation>)
    suspend fun clearPoliticalDonations()

    // ==================== Analysis History ====================

    suspend fun getAnalysisHistory(): List<ChartAnalysisData>
    suspend fun saveAnalysisHistory(analyses: List<ChartAnalysisData>, dateGroup: String = "all")
    suspend fun getAnalysisHistoryGrouped(): List<AnalysisGroupData>
    suspend fun saveAnalysisHistoryGrouped(groups: List<AnalysisGroupData>)
    suspend fun clearAnalysisHistory()

    // ==================== Saved Analyses ====================

    suspend fun getSavedAnalyses(): List<ChartAnalysisData>
    suspend fun saveSavedAnalyses(analyses: List<ChartAnalysisData>)
    suspend fun clearSavedAnalyses()

    // ==================== Trade Ideas ====================

    suspend fun getTradeIdeas(): List<TradeIdea>
    suspend fun saveTradeIdeas(ideas: List<TradeIdea>)
    suspend fun clearTradeIdeas()

    // ==================== Subscription Pricing ====================

    suspend fun getSubscriptionPricing(): List<SubscriptionPricingData>
    suspend fun saveSubscriptionPricing(pricing: List<SubscriptionPricingData>)
    suspend fun clearSubscriptionPricing()

    // ==================== Global Cache Operations ====================

    /**
     * Clear all cached data. Called on user logout.
     */
    suspend fun clearAllCache()

    /**
     * Clear expired cache entries.
     */
    suspend fun clearExpiredCache()
}
