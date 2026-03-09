package com.myopenclaw.data.local

import platform.Foundation.NSUserDefaults

/**
 * iOS implementation of PreferencesManager using NSUserDefaults
 */
class IOSPreferencesManager : PreferencesManager {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    override fun saveAuthToken(token: String) {
        userDefaults.setObject(token, KEY_AUTH_TOKEN)
    }

    override fun getAuthToken(): String? {
        return userDefaults.stringForKey(KEY_AUTH_TOKEN)
    }

    override fun clearAuthToken() {
        userDefaults.removeObjectForKey(KEY_AUTH_TOKEN)
    }

    override fun saveUserId(userId: String) {
        userDefaults.setObject(userId, KEY_USER_ID)
    }

    override fun getUserId(): String? {
        return userDefaults.stringForKey(KEY_USER_ID)
    }

    override fun clearUserId() {
        userDefaults.removeObjectForKey(KEY_USER_ID)
    }

    override fun saveRememberMe(rememberMe: Boolean) {
        userDefaults.setBool(rememberMe, KEY_REMEMBER_ME)
    }

    override fun getRememberMe(): Boolean {
        return userDefaults.boolForKey(KEY_REMEMBER_ME)
    }

    override fun saveNotificationsEnabled(enabled: Boolean) {
        userDefaults.setBool(enabled, KEY_NOTIFICATIONS_ENABLED)
    }

    override fun getNotificationsEnabled(): Boolean {
        val value = userDefaults.objectForKey(KEY_NOTIFICATIONS_ENABLED)
        return if (value != null) {
            userDefaults.boolForKey(KEY_NOTIFICATIONS_ENABLED)
        } else {
            true
        }
    }

    override fun saveDarkModeEnabled(enabled: Boolean) {
        userDefaults.setBool(enabled, KEY_DARK_MODE_ENABLED)
    }

    override fun getDarkModeEnabled(): Boolean {
        val value = userDefaults.objectForKey(KEY_DARK_MODE_ENABLED)
        return if (value != null) {
            userDefaults.boolForKey(KEY_DARK_MODE_ENABLED)
        } else {
            true
        }
    }

    override fun saveHasCompletedOnboarding(completed: Boolean) {
        userDefaults.setBool(completed, KEY_HAS_COMPLETED_ONBOARDING)
    }

    override fun getHasCompletedOnboarding(): Boolean {
        return userDefaults.boolForKey(KEY_HAS_COMPLETED_ONBOARDING)
    }

    override fun saveUserName(name: String) {
        userDefaults.setObject(name, KEY_USER_NAME)
    }

    override fun getUserName(): String? {
        return userDefaults.stringForKey(KEY_USER_NAME)
    }

    override fun saveUserEmail(email: String) {
        userDefaults.setObject(email, KEY_USER_EMAIL)
    }

    override fun getUserEmail(): String? {
        return userDefaults.stringForKey(KEY_USER_EMAIL)
    }

    override fun saveBiggestChallenge(challenge: String) {
        userDefaults.setObject(challenge, KEY_BIGGEST_CHALLENGE)
    }

    override fun getBiggestChallenge(): String? {
        return userDefaults.stringForKey(KEY_BIGGEST_CHALLENGE)
    }

    override fun saveConfidenceLevel(level: String) {
        userDefaults.setObject(level, KEY_CONFIDENCE_LEVEL)
    }

    override fun getConfidenceLevel(): String? {
        return userDefaults.stringForKey(KEY_CONFIDENCE_LEVEL)
    }

    override fun saveAnalysisTime(time: String) {
        userDefaults.setObject(time, KEY_ANALYSIS_TIME)
    }

    override fun getAnalysisTime(): String? {
        return userDefaults.stringForKey(KEY_ANALYSIS_TIME)
    }

    override fun saveChartMindset(mindset: String) {
        userDefaults.setObject(mindset, KEY_CHART_MINDSET)
    }

    override fun getChartMindset(): String? {
        return userDefaults.stringForKey(KEY_CHART_MINDSET)
    }

    override fun saveDesiredHelp(help: String) {
        userDefaults.setObject(help, KEY_DESIRED_HELP)
    }

    override fun getDesiredHelp(): String? {
        return userDefaults.stringForKey(KEY_DESIRED_HELP)
    }

    override fun saveTraderType(type: String) {
        userDefaults.setObject(type, KEY_TRADER_TYPE)
    }

    override fun getTraderType(): String? {
        return userDefaults.stringForKey(KEY_TRADER_TYPE)
    }

    override fun saveTradedAssets(assets: String) {
        userDefaults.setObject(assets, KEY_TRADED_ASSETS)
    }

    override fun getTradedAssets(): String? {
        return userDefaults.stringForKey(KEY_TRADED_ASSETS)
    }

    override fun saveSelectedSubscriptionPlan(plan: String) {
        userDefaults.setObject(plan, KEY_SELECTED_SUBSCRIPTION_PLAN)
    }

    override fun getSelectedSubscriptionPlan(): String? {
        return userDefaults.stringForKey(KEY_SELECTED_SUBSCRIPTION_PLAN)
    }

    override fun saveWantsFreeTrial(wants: Boolean) {
        userDefaults.setBool(wants, KEY_WANTS_FREE_TRIAL)
    }

    override fun getWantsFreeTrial(): Boolean {
        val value = userDefaults.objectForKey(KEY_WANTS_FREE_TRIAL)
        return if (value != null) {
            userDefaults.boolForKey(KEY_WANTS_FREE_TRIAL)
        } else {
            true
        }
    }

    override fun saveWatchlistData(jsonData: String) {
        userDefaults.setObject(jsonData, KEY_WATCHLIST_DATA)
    }

    override fun getWatchlistData(): String? {
        return userDefaults.stringForKey(KEY_WATCHLIST_DATA)
    }

    override fun saveHasUsedFreeAIChat(used: Boolean) {
        userDefaults.setBool(used, KEY_HAS_USED_FREE_AI_CHAT)
    }

    override fun getHasUsedFreeAIChat(): Boolean {
        return userDefaults.boolForKey(KEY_HAS_USED_FREE_AI_CHAT)
    }

    override fun saveFreeChartAnalysisDate(dateString: String) {
        userDefaults.setObject(dateString, KEY_FREE_CHART_ANALYSIS_DATE)
    }

    override fun getFreeChartAnalysisDate(): String? {
        return userDefaults.stringForKey(KEY_FREE_CHART_ANALYSIS_DATE)
    }

    override fun saveFreeChartAnalysisCount(count: Int) {
        userDefaults.setInteger(count.toLong(), KEY_FREE_CHART_ANALYSIS_COUNT)
    }

    override fun getFreeChartAnalysisCount(): Int {
        return userDefaults.integerForKey(KEY_FREE_CHART_ANALYSIS_COUNT).toInt()
    }

    override fun savePriceAlertsData(jsonData: String) {
        userDefaults.setObject(jsonData, KEY_PRICE_ALERTS_DATA)
    }

    override fun getPriceAlertsData(): String? {
        return userDefaults.stringForKey(KEY_PRICE_ALERTS_DATA)
    }

    override fun saveUserRegistrationDate(timestamp: Long) {
        userDefaults.setObject(timestamp, KEY_USER_REGISTRATION_DATE)
    }

    override fun getUserRegistrationDate(): Long? {
        val value = userDefaults.objectForKey(KEY_USER_REGISTRATION_DATE)
        return if (value != null) {
            (value as? Long) ?: userDefaults.integerForKey(KEY_USER_REGISTRATION_DATE).toLong().takeIf { it != 0L }
        } else {
            null
        }
    }

    override fun saveSubscriptionActive(active: Boolean) {
        userDefaults.setBool(active, KEY_SUBSCRIPTION_ACTIVE)
    }

    override fun getSubscriptionActive(): Boolean? {
        val value = userDefaults.objectForKey(KEY_SUBSCRIPTION_ACTIVE)
        return if (value != null) {
            userDefaults.boolForKey(KEY_SUBSCRIPTION_ACTIVE)
        } else null
    }

    override fun saveSubscriptionCheckTimestamp(timestamp: Long) {
        userDefaults.setObject(timestamp, KEY_SUBSCRIPTION_CHECK_TIMESTAMP)
    }

    override fun getSubscriptionCheckTimestamp(): Long {
        return userDefaults.integerForKey(KEY_SUBSCRIPTION_CHECK_TIMESTAMP).toLong()
    }

    override fun saveAIDataConsentGiven(given: Boolean) {
        userDefaults.setBool(given, KEY_AI_DATA_CONSENT)
    }

    override fun getAIDataConsentGiven(): Boolean {
        return userDefaults.boolForKey(KEY_AI_DATA_CONSENT)
    }

    override fun clearAll() {
        ALL_KEYS.forEach { key ->
            userDefaults.removeObjectForKey(key)
        }
    }

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_DARK_MODE_ENABLED = "dark_mode_enabled"
        private const val KEY_HAS_COMPLETED_ONBOARDING = "has_completed_onboarding"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_BIGGEST_CHALLENGE = "biggest_challenge"
        private const val KEY_CONFIDENCE_LEVEL = "confidence_level"
        private const val KEY_ANALYSIS_TIME = "analysis_time"
        private const val KEY_CHART_MINDSET = "chart_mindset"
        private const val KEY_DESIRED_HELP = "desired_help"
        private const val KEY_TRADER_TYPE = "trader_type"
        private const val KEY_TRADED_ASSETS = "traded_assets"
        private const val KEY_SELECTED_SUBSCRIPTION_PLAN = "selected_subscription_plan"
        private const val KEY_WANTS_FREE_TRIAL = "wants_free_trial"
        private const val KEY_WATCHLIST_DATA = "watchlist_data"
        private const val KEY_HAS_USED_FREE_AI_CHAT = "has_used_free_ai_chat"
        private const val KEY_FREE_CHART_ANALYSIS_DATE = "free_chart_analysis_date"
        private const val KEY_FREE_CHART_ANALYSIS_COUNT = "free_chart_analysis_count"
        private const val KEY_PRICE_ALERTS_DATA = "price_alerts_data"
        private const val KEY_USER_REGISTRATION_DATE = "user_registration_date"
        private const val KEY_SUBSCRIPTION_ACTIVE = "subscription_active"
        private const val KEY_SUBSCRIPTION_CHECK_TIMESTAMP = "subscription_check_timestamp"
        private const val KEY_AI_DATA_CONSENT = "ai_data_consent_given"

        private val ALL_KEYS = listOf(
            KEY_AUTH_TOKEN, KEY_USER_ID, KEY_REMEMBER_ME, KEY_NOTIFICATIONS_ENABLED,
            KEY_DARK_MODE_ENABLED, KEY_HAS_COMPLETED_ONBOARDING, KEY_USER_NAME,
            KEY_USER_EMAIL, KEY_BIGGEST_CHALLENGE, KEY_CONFIDENCE_LEVEL,
            KEY_ANALYSIS_TIME, KEY_CHART_MINDSET, KEY_DESIRED_HELP, KEY_TRADER_TYPE,
            KEY_TRADED_ASSETS, KEY_SELECTED_SUBSCRIPTION_PLAN, KEY_WANTS_FREE_TRIAL,
            KEY_WATCHLIST_DATA, KEY_HAS_USED_FREE_AI_CHAT, KEY_USER_REGISTRATION_DATE,
            KEY_SUBSCRIPTION_ACTIVE, KEY_SUBSCRIPTION_CHECK_TIMESTAMP
        )
    }
}
