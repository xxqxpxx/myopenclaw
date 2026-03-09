package com.myopenclaw.data.repository

import com.myopenclaw.data.remote.ApiService
import com.myopenclaw.domain.models.*
import com.myopenclaw.domain.repository.UserRepository
import kotlinx.datetime.Clock

/**
 * Implementation of UserRepository
 */
class UserRepositoryImpl(
    private val apiService: ApiService
) : UserRepository {

    override suspend fun getUserProfile(): Result<UserProfile> {
        return try {
            val response = apiService.getUserProfile()
            if (response.success) {
                Result.success(response.data.toUserProfile())
            } else {
                Result.failure(Exception(response.message ?: "Failed to get user profile"))
            }
        } catch (e: Exception) {
            // println("UserRepositoryImpl: getUserProfile failed - ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updateFullName(fullName: String): Result<UserProfile> {
        return try {
            val request = UpdateProfileRequest(fullName = fullName)
            val response = apiService.updateUserProfile(request)
            if (response.success && response.data != null) {
                Result.success(response.data.toUserProfile())
            } else {
                Result.failure(Exception(response.message ?: "Failed to update full name"))
            }
        } catch (e: Exception) {
            // println("UserRepositoryImpl: updateFullName failed - ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updateLanguage(language: String): Result<UserProfile> {
        return try {
            // Get current profile first to preserve other preferences
            val currentProfile = getUserProfile().getOrNull()
            if (currentProfile == null) {
                return Result.failure(Exception("Failed to get current profile"))
            }

            val updatedPreferences = currentProfile.preferences.copy(language = language)
            val request = UpdateProfileRequest(preferences = updatedPreferences.toUserPreferencesData())
            val response = apiService.updateUserProfile(request)

            if (response.success && response.data != null) {
                Result.success(response.data.toUserProfile())
            } else {
                Result.failure(Exception(response.message ?: "Failed to update language"))
            }
        } catch (e: Exception) {
            // println("UserRepositoryImpl: updateLanguage failed - ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updateNotificationPreferences(
        notificationsEnabled: Boolean,
        marketAlertsEnabled: Boolean
    ): Result<UserProfile> {
        return try {
            // Get current profile first to preserve other preferences
            val currentProfile = getUserProfile().getOrNull()
            if (currentProfile == null) {
                return Result.failure(Exception("Failed to get current profile"))
            }

            val updatedPreferences = currentProfile.preferences.copy(
                notificationsEnabled = notificationsEnabled,
                marketAlertsEnabled = marketAlertsEnabled
            )
            val request = UpdateProfileRequest(preferences = updatedPreferences.toUserPreferencesData())
            val response = apiService.updateUserProfile(request)

            if (response.success && response.data != null) {
                Result.success(response.data.toUserProfile())
            } else {
                Result.failure(Exception(response.message ?: "Failed to update notifications"))
            }
        } catch (e: Exception) {
            // println("UserRepositoryImpl: updateNotificationPreferences failed - ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updateTheme(theme: String): Result<UserProfile> {
        return try {
            // Get current profile first to preserve other preferences
            val currentProfile = getUserProfile().getOrNull()
            if (currentProfile == null) {
                return Result.failure(Exception("Failed to get current profile"))
            }

            val updatedPreferences = currentProfile.preferences.copy(theme = theme)
            val request = UpdateProfileRequest(preferences = updatedPreferences.toUserPreferencesData())
            val response = apiService.updateUserProfile(request)

            if (response.success && response.data != null) {
                Result.success(response.data.toUserProfile())
            } else {
                Result.failure(Exception(response.message ?: "Failed to update theme"))
            }
        } catch (e: Exception) {
            // println("UserRepositoryImpl: updateTheme failed - ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getSubscriptionPricing(): Result<List<SubscriptionPricing>> {
        return try {
            val response = apiService.getSubscriptionPricing()
            if (response.success) {
                Result.success(response.data.map { it.toSubscriptionPricing() })
            } else {
                Result.failure(Exception(response.message ?: "Failed to get pricing"))
            }
        } catch (e: Exception) {
            // println("UserRepositoryImpl: getSubscriptionPricing failed - ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deleteAccount(
        reason: String?,
        feedback: String?,
        confirmEmail: String
    ): Result<Unit> {
        return try {
            val request = DeleteAccountRequest(
                reason = reason,
                feedback = feedback,
                confirmEmail = confirmEmail
            )
            val response = apiService.deleteUserAccount(request)
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            // println("UserRepositoryImpl: deleteAccount failed - ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun syncOnboardingData(
        traderType: String?,
        tradedAssets: String?,
        biggestChallenge: String?,
        confidenceLevel: String?,
        analysisTime: String?,
        chartMindset: String?,
        desiredHelp: String?,
        completedAt: String?
    ): Result<Unit> {
        return try {
            // println("UserRepositoryImpl: Syncing onboarding data to backend...")
            val onboardingData = OnboardingDataRequest(
                traderType = traderType,
                tradedAssets = tradedAssets,
                biggestChallenge = biggestChallenge,
                confidenceLevel = confidenceLevel,
                analysisTime = analysisTime,
                chartMindset = chartMindset,
                desiredHelp = desiredHelp,
                completedAt = completedAt ?: Clock.System.now().toString()
            )
            val request = UpdateProfileRequest(onboarding = onboardingData)
            val response = apiService.updateUserProfile(request)

            if (response.success) {
                // println("UserRepositoryImpl: Onboarding data synced successfully")
                Result.success(Unit)
            } else {
                // println("UserRepositoryImpl: Failed to sync onboarding data - ${response.message}")
                Result.failure(Exception(response.message ?: "Failed to sync onboarding data"))
            }
        } catch (e: Exception) {
            // println("UserRepositoryImpl: syncOnboardingData failed - ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
