package com.myopenclaw.ui.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myopenclaw.data.local.PreferencesManager
import com.myopenclaw.data.local.cache.CacheManager
import com.myopenclaw.data.session.SessionManager
import com.myopenclaw.domain.models.UserProfile
import com.myopenclaw.domain.models.SubscriptionPricing
import com.myopenclaw.domain.models.SubscriptionType
import com.myopenclaw.domain.models.SubscriptionInfo
import com.myopenclaw.domain.models.SubscriptionStatus
import com.myopenclaw.domain.models.UserPreferences
import com.myopenclaw.domain.repository.AuthRepository
import com.myopenclaw.domain.repository.UserRepository
import com.myopenclaw.domain.repository.RevenueCatRepository
import com.myopenclaw.ui.screens.profile.SubscriptionPlan
import com.myopenclaw.util.LocalizationManager
import com.myopenclaw.util.StringKeys
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * UI States for Profile Screen
 */
sealed class ProfileState {
    data object Loading : ProfileState()
    data class Success(val profile: UserProfile) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

sealed class PricingState {
    data object Idle : PricingState()
    data object Loading : PricingState()
    data class Success(val pricing: List<SubscriptionPricing>) : PricingState()
    data class Error(val message: String) : PricingState()
}

sealed class UpdateState {
    data object Idle : UpdateState()
    data object Loading : UpdateState()
    data object Success : UpdateState()
    data class Error(val message: String) : UpdateState()
}

/**
 * ViewModel for Profile screens
 */
class ProfileViewModel(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val preferencesManager: PreferencesManager,
    private val sessionManager: SessionManager,
    private val revenueCatRepository: RevenueCatRepository? = null,
    private val cacheManager: CacheManager? = null
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private val _pricingState = MutableStateFlow<PricingState>(PricingState.Idle)
    val pricingState: StateFlow<PricingState> = _pricingState.asStateFlow()

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val _deleteAccountState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val deleteAccountState: StateFlow<UpdateState> = _deleteAccountState.asStateFlow()

    private val _selectedUpgradePlan = MutableStateFlow<SubscriptionPlan?>(null)
    val selectedUpgradePlan: StateFlow<SubscriptionPlan?> = _selectedUpgradePlan.asStateFlow()

    // Track RevenueCat subscription status
    private val _hasRevenueCatSubscription = MutableStateFlow(false)
    val hasRevenueCatSubscription: StateFlow<Boolean> = _hasRevenueCatSubscription.asStateFlow()

    init {
        loadUserProfile()
        checkInitialSubscriptionStatus()
    }

    /**
     * Initialize localization from profile when profile is loaded
     */
    private fun initializeLocalization(profile: UserProfile) {
        LocalizationManager.setLanguage(profile.preferences.language)
    }

    /**
     * Check subscription status on init
     */
    private fun checkInitialSubscriptionStatus() {
        viewModelScope.launch {
            try {
                if (revenueCatRepository != null) {
                    val hasActiveSub = revenueCatRepository.hasActiveSubscription()
                    _hasRevenueCatSubscription.value = hasActiveSub
                    // println("ProfileViewModel: Initial RevenueCat subscription check: $hasActiveSub")
                }
            } catch (e: Exception) {
                // println("ProfileViewModel: Error on initial subscription check - ${e.message}")
            }
        }
    }

    /**
     * Load user profile from backend
     * Falls back to a default profile if the API call fails
     */
    fun loadUserProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            userRepository.getUserProfile()
                .onSuccess { profile ->
                    _profileState.value = ProfileState.Success(profile)
                    initializeLocalization(profile)
                }
                .onFailure { error ->
                    // println("ProfileViewModel: Failed to load profile from API - ${error.message}")
                    // Use fallback profile for better UX when API is unavailable
                    val fallbackProfile = createFallbackProfile()
                    _profileState.value = ProfileState.Success(fallbackProfile)
                    initializeLocalization(fallbackProfile)
                }
        }
    }

    /**
     * Create a fallback profile using Firebase Auth current user data
     * when the API is unavailable. Prefers live Firebase data over
     * cached preferences to avoid showing stale data from previous sessions.
     */
    private fun createFallbackProfile(): UserProfile {
        // Primary source: Firebase Auth current user (always accurate for signed-in user)
        val firebaseUser = authRepository.authState.value
        val firebaseEmail = firebaseUser?.email?.takeIf { it.isNotBlank() }
        val firebaseName = firebaseUser?.displayName?.takeIf { it.isNotBlank() }

        // Secondary source: cached preferences (may be stale)
        val cachedEmail = sessionManager.getCurrentUserEmail()?.takeIf { it.isNotBlank() }
        val cachedName = preferencesManager.getUserName()?.takeIf { it.isNotBlank() }

        // Use Firebase data first, fall back to cached data
        val email = firebaseEmail ?: cachedEmail ?: ""
        val displayName = firebaseName
            ?: cachedName
            ?: email.substringBefore("@").takeIf { it.isNotBlank() }
            ?: "User"

        return UserProfile(
            id = firebaseUser?.uid ?: "local_user",
            fullName = displayName,
            email = email,
            joinedDate = "",
            subscription = SubscriptionInfo(
                type = SubscriptionType.FREE,
                status = SubscriptionStatus.ACTIVE,
                planType = null,
                billingCycle = null,
                nextBillingDate = null,
                trialEndsIn = null
            ),
            preferences = UserPreferences(
                language = "English",
                notificationsEnabled = true,
                marketAlertsEnabled = true,
                theme = "dark"
            )
        )
    }

    /**
     * Load subscription pricing plans
     */
    fun loadSubscriptionPricing() {
        viewModelScope.launch {
            _pricingState.value = PricingState.Loading
            userRepository.getSubscriptionPricing()
                .onSuccess { pricing ->
                    _pricingState.value = PricingState.Success(pricing)
                }
                .onFailure { error ->
                    _pricingState.value = PricingState.Error(
                        error.message ?: "Failed to load pricing"
                    )
                }
        }
    }

    /**
     * Update user's full name
     */
    fun updateFullName(fullName: String) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            userRepository.updateFullName(fullName)
                .onSuccess { profile ->
                    _profileState.value = ProfileState.Success(profile)
                    _updateState.value = UpdateState.Success
                }
                .onFailure { error ->
                    _updateState.value = UpdateState.Error(
                        error.message ?: "Failed to update name"
                    )
                }
        }
    }

    /**
     * Update user's language preference
     */
    fun updateLanguage(language: String) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            userRepository.updateLanguage(language)
                .onSuccess { profile ->
                    _profileState.value = ProfileState.Success(profile)
                    // Update localization manager immediately
                    LocalizationManager.setLanguage(language)
                    _updateState.value = UpdateState.Success
                }
                .onFailure { error ->
                    _updateState.value = UpdateState.Error(
                        error.message ?: "Failed to update language"
                    )
                }
        }
    }

    /**
     * Update notification preferences
     */
    fun updateNotificationPreferences(
        notificationsEnabled: Boolean,
        marketAlertsEnabled: Boolean
    ) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            userRepository.updateNotificationPreferences(
                notificationsEnabled = notificationsEnabled,
                marketAlertsEnabled = marketAlertsEnabled
            )
                .onSuccess { profile ->
                    _profileState.value = ProfileState.Success(profile)
                    _updateState.value = UpdateState.Success
                }
                .onFailure { error ->
                    _updateState.value = UpdateState.Error(
                        error.message ?: "Failed to update preferences"
                    )
                }
        }
    }

    /**
     * Update theme preference
     */
    fun updateTheme(theme: String) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            userRepository.updateTheme(theme)
                .onSuccess { profile ->
                    _profileState.value = ProfileState.Success(profile)
                    _updateState.value = UpdateState.Success
                }
                .onFailure { error ->
                    _updateState.value = UpdateState.Error(
                        error.message ?: "Failed to update theme"
                    )
                }
        }
    }

    /**
     * Sign out the current user
     */
    fun signOut(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
                .onSuccess {
                    // Clear all session data from preferences
                    clearUserSession()
                    onSuccess()
                }
                .onFailure { error ->
                    _updateState.value = UpdateState.Error(
                        error.message ?: "Failed to sign out"
                    )
                }
        }
    }

    /**
     * Clear all local user data on sign-out.
     * Clears session, preferences, and cached database data.
     */
    private fun clearUserSession() {
        // Use SessionManager to clear session properly (also logs out RevenueCat)
        sessionManager.clearSession()
        // Clear all preferences (user info, watchlist, alerts, usage tracking, etc.)
        preferencesManager.clearAll()
        // Clear all cached database data (profile, signals, analyses, etc.)
        viewModelScope.launch {
            try {
                cacheManager?.clearAllCache()
            } catch (_: Exception) {
                // Don't block sign-out if cache clear fails
            }
        }
    }

    /**
     * Delete user account
     */
    fun deleteAccount(
        reason: String?,
        feedback: String?,
        confirmEmail: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _deleteAccountState.value = UpdateState.Loading
            userRepository.deleteAccount(reason, feedback, confirmEmail)
                .onSuccess {
                    _deleteAccountState.value = UpdateState.Success
                    // Also sign out and clear session
                    authRepository.signOut()
                    clearUserSession()
                    // Clear onboarding too since account is deleted
                    preferencesManager.saveHasCompletedOnboarding(false)
                    onSuccess()
                }
                .onFailure { error ->
                    _deleteAccountState.value = UpdateState.Error(
                        error.message ?: "Failed to delete account"
                    )
                }
        }
    }

    /**
     * Select an upgrade plan
     */
    fun selectUpgradePlan(plan: SubscriptionPlan) {
        _selectedUpgradePlan.value = plan
    }

    /**
     * Clear selected upgrade plan
     */
    fun clearSelectedPlan() {
        _selectedUpgradePlan.value = null
    }

    /**
     * Reset update state
     */
    fun resetUpdateState() {
        _updateState.value = UpdateState.Idle
    }

    /**
     * Reset delete account state
     */
    fun resetDeleteAccountState() {
        _deleteAccountState.value = UpdateState.Idle
    }

    // ==================== Helper Functions ====================

    /**
     * Check if user is a paid subscriber
     * First checks RevenueCat, falls back to backend subscription info if unavailable
     */
    fun isPaidSubscriber(): Boolean {
        // Check RevenueCat subscription first (source of truth for in-app purchases)
        if (_hasRevenueCatSubscription.value) {
            return true
        }
        // Fall back to backend subscription info
        val state = _profileState.value
        return if (state is ProfileState.Success) {
            state.profile.subscription.type == SubscriptionType.PREMIUM
        } else {
            false
        }
    }

    /**
     * Refresh subscription status from RevenueCat
     * This should be called when the subscription screen is shown or after a purchase
     */
    fun refreshSubscriptionStatus() {
        viewModelScope.launch {
            try {
                if (revenueCatRepository != null) {
                    val hasActiveSub = revenueCatRepository.hasActiveSubscription()
                    _hasRevenueCatSubscription.value = hasActiveSub
                    // println("ProfileViewModel: RevenueCat subscription status: $hasActiveSub")

                    // Update profile state to reflect subscription status
                    if (hasActiveSub) {
                        val currentState = _profileState.value
                        if (currentState is ProfileState.Success) {
                            val updatedProfile = currentState.profile.copy(
                                subscription = currentState.profile.subscription.copy(
                                    type = SubscriptionType.PREMIUM,
                                    status = SubscriptionStatus.ACTIVE
                                )
                            )
                            _profileState.value = ProfileState.Success(updatedProfile)
                            // println("ProfileViewModel: Updated profile to PREMIUM based on RevenueCat")
                        }
                    }
                } else {
                    // println("ProfileViewModel: RevenueCat repository not available")
                }
            } catch (e: Exception) {
                // println("ProfileViewModel: Error checking RevenueCat subscription - ${e.message}")
            }
        }
    }

    /**
     * Get formatted billing info text
     * Uses RevenueCat data when available, falls back to backend subscription info
     */
    fun getBillingInfo(): String {
        val state = _profileState.value
        if (state !is ProfileState.Success) return ""
        
        val language = state.profile.preferences.language
        val translations = getTranslationsForLanguage(language)

        // Check RevenueCat subscription first
        if (_hasRevenueCatSubscription.value) {
            return translations[StringKeys.PROFILE_ACTIVE_SUBSCRIPTION] ?: "Active subscription"
        }

        val subscription = state.profile.subscription
        return when (subscription.type) {
            SubscriptionType.FREE -> {
                subscription.trialEndsIn?.let { days ->
                    (translations[StringKeys.PROFILE_BILLING_STARTS_IN] ?: "Billing starts in %d days")
                        .replace("%d", days.toString())
                } ?: (translations[StringKeys.PROFILE_FREE_ACCOUNT] ?: "Free account")
            }
            SubscriptionType.TRIAL -> {
                subscription.trialEndsIn?.let { days ->
                    (translations[StringKeys.PROFILE_TRIAL_ENDS_IN] ?: "Trial ends in %d days")
                        .replace("%d", days.toString())
                } ?: (translations[StringKeys.PROFILE_TRIAL_ACCOUNT] ?: "Trial account")
            }
            SubscriptionType.PREMIUM -> {
                subscription.nextBillingDate?.let { date ->
                    (translations[StringKeys.PROFILE_NEXT_BILLING] ?: "Next billing %s")
                        .replace("%s", date)
                } ?: (translations[StringKeys.PROFILE_ACTIVE_SUBSCRIPTION] ?: "Active subscription")
            }
        }
    }

    /**
     * Get translations map for a specific language
     */
    private fun getTranslationsForLanguage(language: String): Map<String, String> {
        return when (language) {
            "Spanish" -> com.myopenclaw.util.SpanishTranslations.strings
            "French" -> com.myopenclaw.util.FrenchTranslations.strings
            "German" -> com.myopenclaw.util.GermanTranslations.strings
            "Portuguese" -> com.myopenclaw.util.PortugueseTranslations.strings
            "Italian" -> com.myopenclaw.util.ItalianTranslations.strings
            "Japanese" -> com.myopenclaw.util.JapaneseTranslations.strings
            "Chinese (Simplified)" -> com.myopenclaw.util.ChineseTranslations.strings
            "Korean" -> com.myopenclaw.util.KoreanTranslations.strings
            "Arabic" -> com.myopenclaw.util.ArabicTranslations.strings
            else -> com.myopenclaw.util.EnglishTranslations.strings
        }
    }

    /**
     * Get user initials for avatar
     */
    fun getUserInitials(): String {
        val state = _profileState.value
        if (state !is ProfileState.Success) return ""

        return state.profile.fullName
            .split(" ")
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")
    }

    /**
     * Format joined date nicely (e.g. "Joined Feb 2026")
     */
    fun getFormattedJoinedDate(): String {
        val state = _profileState.value
        if (state !is ProfileState.Success) return ""

        val raw = state.profile.joinedDate
        if (raw.isBlank()) return ""

        // Try to parse common date formats and return "Joined Mon YYYY"
        return try {
            // Handle "Wed, 25 Feb 2026 22:26:35 GMT" format
            val parts = raw.replace(",", "").split(" ").filter { it.isNotBlank() }
            if (parts.size >= 4) {
                val monthStr = parts.find { it.length == 3 && it.first().isLetter() && it != parts.firstOrNull() }
                val yearStr = parts.find { it.length == 4 && it.all { c -> c.isDigit() } }
                if (monthStr != null && yearStr != null) {
                    "Joined $monthStr $yearStr"
                } else {
                    "Joined $raw"
                }
            } else {
                "Joined $raw"
            }
        } catch (_: Exception) {
            "Joined $raw"
        }
    }
}
