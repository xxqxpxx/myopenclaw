package com.myopenclaw.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Android implementation of PreferencesManager using SharedPreferences
 */
class AndroidPreferencesManager(context: Context) : PreferencesManager {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "myopenclaw_prefs",
        Context.MODE_PRIVATE
    )

    override fun saveAuthToken(token: String) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    override fun getAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    override fun clearAuthToken() {
        prefs.edit().remove(KEY_AUTH_TOKEN).apply()
    }

    override fun saveUserId(userId: String) {
        prefs.edit().putString(KEY_USER_ID, userId).apply()
    }

    override fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    override fun clearUserId() {
        prefs.edit().remove(KEY_USER_ID).apply()
    }

    override fun saveRememberMe(rememberMe: Boolean) {
        prefs.edit().putBoolean(KEY_REMEMBER_ME, rememberMe).apply()
    }

    override fun getRememberMe(): Boolean {
        return prefs.getBoolean(KEY_REMEMBER_ME, false)
    }

    override fun saveNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    override fun getNotificationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    override fun saveDarkModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE_ENABLED, enabled).apply()
    }

    override fun getDarkModeEnabled(): Boolean {
        return prefs.getBoolean(KEY_DARK_MODE_ENABLED, true)
    }

    override fun saveHasCompletedOnboarding(completed: Boolean) {
        prefs.edit().putBoolean(KEY_HAS_COMPLETED_ONBOARDING, completed).apply()
    }

    override fun getHasCompletedOnboarding(): Boolean {
        return prefs.getBoolean(KEY_HAS_COMPLETED_ONBOARDING, false)
    }

    override fun clearAll() {
        prefs.edit().clear().apply()
    }

    override fun saveUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name).apply()
    }

    override fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    override fun saveUserEmail(email: String) {
        prefs.edit().putString(KEY_USER_EMAIL, email).apply()
    }

    override fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    override fun saveBiggestChallenge(challenge: String) {
        prefs.edit().putString(KEY_BIGGEST_CHALLENGE, challenge).apply()
    }

    override fun getBiggestChallenge(): String? {
        return prefs.getString(KEY_BIGGEST_CHALLENGE, null)
    }

    override fun saveConfidenceLevel(level: String) {
        prefs.edit().putString(KEY_CONFIDENCE_LEVEL, level).apply()
    }

    override fun getConfidenceLevel(): String? {
        return prefs.getString(KEY_CONFIDENCE_LEVEL, null)
    }

    override fun saveAnalysisTime(time: String) {
        prefs.edit().putString(KEY_ANALYSIS_TIME, time).apply()
    }

    override fun getAnalysisTime(): String? {
        return prefs.getString(KEY_ANALYSIS_TIME, null)
    }

    override fun saveChartMindset(mindset: String) {
        prefs.edit().putString(KEY_CHART_MINDSET, mindset).apply()
    }

    override fun getChartMindset(): String? {
        return prefs.getString(KEY_CHART_MINDSET, null)
    }

    override fun saveDesiredHelp(help: String) {
        prefs.edit().putString(KEY_DESIRED_HELP, help).apply()
    }

    override fun getDesiredHelp(): String? {
        return prefs.getString(KEY_DESIRED_HELP, null)
    }

    override fun saveTraderType(type: String) {
        prefs.edit().putString(KEY_TRADER_TYPE, type).apply()
    }

    override fun getTraderType(): String? {
        return prefs.getString(KEY_TRADER_TYPE, null)
    }

    override fun saveTradedAssets(assets: String) {
        prefs.edit().putString(KEY_TRADED_ASSETS, assets).apply()
    }

    override fun getTradedAssets(): String? {
        return prefs.getString(KEY_TRADED_ASSETS, null)
    }

    override fun saveSelectedSubscriptionPlan(plan: String) {
        prefs.edit().putString(KEY_SELECTED_SUBSCRIPTION_PLAN, plan).apply()
    }

    override fun getSelectedSubscriptionPlan(): String? {
        return prefs.getString(KEY_SELECTED_SUBSCRIPTION_PLAN, null)
    }

    override fun saveWantsFreeTrial(wants: Boolean) {
        prefs.edit().putBoolean(KEY_WANTS_FREE_TRIAL, wants).apply()
    }

    override fun getWantsFreeTrial(): Boolean {
        return prefs.getBoolean(KEY_WANTS_FREE_TRIAL, false)
    }

    override fun saveWatchlistData(jsonData: String) {
        prefs.edit().putString(KEY_WATCHLIST_DATA, jsonData).apply()
    }

    override fun getWatchlistData(): String? {
        return prefs.getString(KEY_WATCHLIST_DATA, null)
    }

    override fun saveHasUsedFreeAIChat(used: Boolean) {
        prefs.edit().putBoolean(KEY_HAS_USED_FREE_AI_CHAT, used).apply()
    }

    override fun getHasUsedFreeAIChat(): Boolean {
        return prefs.getBoolean(KEY_HAS_USED_FREE_AI_CHAT, false)
    }

    override fun saveFreeChartAnalysisDate(dateString: String) {
        prefs.edit().putString(KEY_FREE_CHART_ANALYSIS_DATE, dateString).apply()
    }

    override fun getFreeChartAnalysisDate(): String? {
        return prefs.getString(KEY_FREE_CHART_ANALYSIS_DATE, null)
    }

    override fun saveFreeChartAnalysisCount(count: Int) {
        prefs.edit().putInt(KEY_FREE_CHART_ANALYSIS_COUNT, count).apply()
    }

    override fun getFreeChartAnalysisCount(): Int {
        return prefs.getInt(KEY_FREE_CHART_ANALYSIS_COUNT, 0)
    }

    override fun savePriceAlertsData(jsonData: String) {
        prefs.edit().putString(KEY_PRICE_ALERTS_DATA, jsonData).apply()
    }

    override fun getPriceAlertsData(): String? {
        return prefs.getString(KEY_PRICE_ALERTS_DATA, null)
    }

    override fun saveUserRegistrationDate(timestamp: Long) {
        prefs.edit().putLong(KEY_USER_REGISTRATION_DATE, timestamp).apply()
    }

    override fun getUserRegistrationDate(): Long? {
        val value = prefs.getLong(KEY_USER_REGISTRATION_DATE, 0L)
        return if (value != 0L) value else null
    }

    override fun saveSubscriptionActive(active: Boolean) {
        prefs.edit().putBoolean(KEY_SUBSCRIPTION_ACTIVE, active).apply()
    }

    override fun getSubscriptionActive(): Boolean? {
        return if (prefs.contains(KEY_SUBSCRIPTION_ACTIVE)) {
            prefs.getBoolean(KEY_SUBSCRIPTION_ACTIVE, false)
        } else null
    }

    override fun saveSubscriptionCheckTimestamp(timestamp: Long) {
        prefs.edit().putLong(KEY_SUBSCRIPTION_CHECK_TIMESTAMP, timestamp).apply()
    }

    override fun getSubscriptionCheckTimestamp(): Long {
        return prefs.getLong(KEY_SUBSCRIPTION_CHECK_TIMESTAMP, 0L)
    }

    override fun saveAIDataConsentGiven(given: Boolean) {
        prefs.edit().putBoolean(KEY_AI_DATA_CONSENT, given).apply()
    }

    override fun getAIDataConsentGiven(): Boolean {
        return prefs.getBoolean(KEY_AI_DATA_CONSENT, false)
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
    }
}
