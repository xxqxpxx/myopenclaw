package com.myopenclaw.domain.repository

import com.myopenclaw.domain.models.UserProfile
import com.myopenclaw.domain.models.SubscriptionPricing

/**
 * Repository for user profile operations
 */
interface UserRepository {
    /**
     * Get the current user's profile
     */
    suspend fun getUserProfile(): Result<UserProfile>

    /**
     * Update user's full name
     */
    suspend fun updateFullName(fullName: String): Result<UserProfile>

    /**
     * Update user's language preference
     */
    suspend fun updateLanguage(language: String): Result<UserProfile>

    /**
     * Update user's notification preferences
     */
    suspend fun updateNotificationPreferences(
        notificationsEnabled: Boolean,
        marketAlertsEnabled: Boolean
    ): Result<UserProfile>

    /**
     * Update user's theme preference
     */
    suspend fun updateTheme(theme: String): Result<UserProfile>

    /**
     * Get available subscription pricing plans
     */
    suspend fun getSubscriptionPricing(): Result<List<SubscriptionPricing>>

    /**
     * Delete user account
     */
    suspend fun deleteAccount(
        reason: String?,
        feedback: String?,
        confirmEmail: String
    ): Result<Unit>

    /**
     * Sync onboarding questionnaire answers to the backend
     */
    suspend fun syncOnboardingData(
        traderType: String?,
        tradedAssets: String?,
        biggestChallenge: String?,
        confidenceLevel: String?,
        analysisTime: String?,
        chartMindset: String?,
        desiredHelp: String?,
        completedAt: String? = null
    ): Result<Unit>
}
