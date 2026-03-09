package com.myopenclaw.data.local

/**
 * Platform-agnostic preferences manager for storing user settings.
 * Uses SharedPreferences on Android and UserDefaults on iOS.
 */
interface PreferencesManager {
    /**
     * Authentication preferences
     */
    fun saveAuthToken(token: String)
    fun getAuthToken(): String?
    fun clearAuthToken()

    fun saveUserId(userId: String)
    fun getUserId(): String?
    fun clearUserId()

    /**
     * User preferences
     */
    fun saveRememberMe(rememberMe: Boolean)
    fun getRememberMe(): Boolean

    fun saveNotificationsEnabled(enabled: Boolean)
    fun getNotificationsEnabled(): Boolean

    fun saveDarkModeEnabled(enabled: Boolean)
    fun getDarkModeEnabled(): Boolean

    /**
     * Onboarding
     */
    fun saveHasCompletedOnboarding(completed: Boolean)
    fun getHasCompletedOnboarding(): Boolean

    /**
     * Onboarding Responses - User Profile Data
     */
    fun saveUserName(name: String)
    fun getUserName(): String?

    fun saveUserEmail(email: String)
    fun getUserEmail(): String?

    // Questionnaire responses
    fun saveBiggestChallenge(challenge: String)
    fun getBiggestChallenge(): String?

    fun saveConfidenceLevel(level: String)
    fun getConfidenceLevel(): String?

    fun saveAnalysisTime(time: String)
    fun getAnalysisTime(): String?

    fun saveChartMindset(mindset: String)
    fun getChartMindset(): String?

    fun saveDesiredHelp(help: String)
    fun getDesiredHelp(): String?

    fun saveTraderType(type: String)
    fun getTraderType(): String?

    fun saveTradedAssets(assets: String)
    fun getTradedAssets(): String?

    // Subscription choices
    fun saveSelectedSubscriptionPlan(plan: String)
    fun getSelectedSubscriptionPlan(): String?

    fun saveWantsFreeTrial(wants: Boolean)
    fun getWantsFreeTrial(): Boolean

    /**
     * Watchlist preferences - stores as JSON string
     */
    fun saveWatchlistData(jsonData: String)
    fun getWatchlistData(): String?

    /**
     * AI Chat free usage tracking
     */
    fun saveHasUsedFreeAIChat(used: Boolean)
    fun getHasUsedFreeAIChat(): Boolean

    /**
     * User registration date tracking for trial period
     */
    fun saveUserRegistrationDate(timestamp: Long)
    fun getUserRegistrationDate(): Long?

    /**
     * AI data sharing consent (required by Apple 5.1.1)
     */
    fun saveAIDataConsentGiven(given: Boolean)
    fun getAIDataConsentGiven(): Boolean

    /**
     * Subscription status cache (persisted across app restarts)
     */
    fun saveSubscriptionActive(active: Boolean)
    fun getSubscriptionActive(): Boolean?
    fun saveSubscriptionCheckTimestamp(timestamp: Long)
    fun getSubscriptionCheckTimestamp(): Long

    /**
     * Clear all preferences
     */
    fun clearAll()
}

/**
 * Mock implementation for development/testing
 */
class MockPreferencesManager : PreferencesManager {
    private val storage = mutableMapOf<String, Any>()

    override fun saveAuthToken(token: String) {
        storage["auth_token"] = token
    }

    override fun getAuthToken(): String? {
        return storage["auth_token"] as? String
    }

    override fun clearAuthToken() {
        storage.remove("auth_token")
    }

    override fun saveUserId(userId: String) {
        storage["user_id"] = userId
    }

    override fun getUserId(): String? {
        return storage["user_id"] as? String
    }

    override fun clearUserId() {
        storage.remove("user_id")
    }

    override fun saveRememberMe(rememberMe: Boolean) {
        storage["remember_me"] = rememberMe
    }

    override fun getRememberMe(): Boolean {
        return storage["remember_me"] as? Boolean ?: false
    }

    override fun saveNotificationsEnabled(enabled: Boolean) {
        storage["notifications_enabled"] = enabled
    }

    override fun getNotificationsEnabled(): Boolean {
        return storage["notifications_enabled"] as? Boolean ?: true
    }

    override fun saveDarkModeEnabled(enabled: Boolean) {
        storage["dark_mode_enabled"] = enabled
    }

    override fun getDarkModeEnabled(): Boolean {
        return storage["dark_mode_enabled"] as? Boolean ?: true
    }

    override fun saveHasCompletedOnboarding(completed: Boolean) {
        storage["has_completed_onboarding"] = completed
    }

    override fun getHasCompletedOnboarding(): Boolean {
        return storage["has_completed_onboarding"] as? Boolean ?: false
    }

    override fun saveUserName(name: String) {
        storage["user_name"] = name
    }

    override fun getUserName(): String? {
        return storage["user_name"] as? String
    }

    override fun saveUserEmail(email: String) {
        storage["user_email"] = email
    }

    override fun getUserEmail(): String? {
        return storage["user_email"] as? String
    }

    override fun saveBiggestChallenge(challenge: String) {
        storage["biggest_challenge"] = challenge
    }

    override fun getBiggestChallenge(): String? {
        return storage["biggest_challenge"] as? String
    }

    override fun saveConfidenceLevel(level: String) {
        storage["confidence_level"] = level
    }

    override fun getConfidenceLevel(): String? {
        return storage["confidence_level"] as? String
    }

    override fun saveAnalysisTime(time: String) {
        storage["analysis_time"] = time
    }

    override fun getAnalysisTime(): String? {
        return storage["analysis_time"] as? String
    }

    override fun saveChartMindset(mindset: String) {
        storage["chart_mindset"] = mindset
    }

    override fun getChartMindset(): String? {
        return storage["chart_mindset"] as? String
    }

    override fun saveDesiredHelp(help: String) {
        storage["desired_help"] = help
    }

    override fun getDesiredHelp(): String? {
        return storage["desired_help"] as? String
    }

    override fun saveTraderType(type: String) {
        storage["trader_type"] = type
    }

    override fun getTraderType(): String? {
        return storage["trader_type"] as? String
    }

    override fun saveTradedAssets(assets: String) {
        storage["traded_assets"] = assets
    }

    override fun getTradedAssets(): String? {
        return storage["traded_assets"] as? String
    }

    override fun saveSelectedSubscriptionPlan(plan: String) {
        storage["selected_plan"] = plan
    }

    override fun getSelectedSubscriptionPlan(): String? {
        return storage["selected_plan"] as? String
    }

    override fun saveWantsFreeTrial(wants: Boolean) {
        storage["wants_free_trial"] = wants
    }

    override fun getWantsFreeTrial(): Boolean {
        return storage["wants_free_trial"] as? Boolean ?: true
    }

    override fun saveWatchlistData(jsonData: String) {
        storage["watchlist_data"] = jsonData
    }

    override fun getWatchlistData(): String? {
        return storage["watchlist_data"] as? String
    }

    override fun saveHasUsedFreeAIChat(used: Boolean) {
        storage["has_used_free_ai_chat"] = used
    }

    override fun getHasUsedFreeAIChat(): Boolean {
        return storage["has_used_free_ai_chat"] as? Boolean ?: false
    }

    override fun saveUserRegistrationDate(timestamp: Long) {
        storage["user_registration_date"] = timestamp
    }

    override fun getUserRegistrationDate(): Long? {
        return storage["user_registration_date"] as? Long
    }

    override fun saveAIDataConsentGiven(given: Boolean) {
        storage["ai_data_consent_given"] = given
    }

    override fun getAIDataConsentGiven(): Boolean {
        return storage["ai_data_consent_given"] as? Boolean ?: false
    }

    override fun saveSubscriptionActive(active: Boolean) {
        storage["subscription_active"] = active
    }

    override fun getSubscriptionActive(): Boolean? {
        return storage["subscription_active"] as? Boolean
    }

    override fun saveSubscriptionCheckTimestamp(timestamp: Long) {
        storage["subscription_check_timestamp"] = timestamp
    }

    override fun getSubscriptionCheckTimestamp(): Long {
        return storage["subscription_check_timestamp"] as? Long ?: 0L
    }

    override fun clearAll() {
        storage.clear()
    }
}
