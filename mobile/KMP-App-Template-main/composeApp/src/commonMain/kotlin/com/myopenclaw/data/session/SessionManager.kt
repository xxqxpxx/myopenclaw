package com.myopenclaw.data.session

import com.myopenclaw.data.auth.AuthManager
import com.myopenclaw.data.local.PreferencesManager
import com.myopenclaw.data.remote.RevenueCatConfig
import com.myopenclaw.domain.models.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Session state representing the current authentication status
 */
sealed class SessionState {
    /** Initial loading state while checking session */
    data object Loading : SessionState()

    /** User is authenticated with a valid session */
    data class Authenticated(val user: User) : SessionState()

    /** No valid session found, user needs to sign in */
    data object Unauthenticated : SessionState()
}

/**
 * Manages user session persistence and restoration.
 *
 * Coordinates between Firebase Auth and local preferences to ensure
 * seamless auto-login experience across app restarts.
 */
class SessionManager(
    private val authManager: AuthManager,
    private val preferencesManager: PreferencesManager
) {
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Loading)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    /**
     * Check and restore session on app startup.
     *
     * This should be called from the splash screen to determine
     * the initial navigation destination.
     */
    suspend fun checkSession(): SessionState {
        // println("SessionManager: Checking session...")

        // Give Firebase SDK a moment to initialize and restore session
        // This handles the case where Firebase hasn't fully initialized yet
        val maxRetries = 3
        var retryCount = 0

        while (retryCount < maxRetries) {
            // Check if Firebase has a current user
            val currentUser = authManager.getCurrentUser()

            if (currentUser != null) {
                // println("SessionManager: Found authenticated user: ${currentUser.email}")

                // Validate the session by trying to get a fresh token
                val token = authManager.getIdToken(forceRefresh = false)

                if (token != null) {
                    // println("SessionManager: Token valid, session restored")

                    // Ensure preferences are up-to-date
                    saveSessionToPreferences(currentUser, token)

                    // Sync user with RevenueCat for subscription tracking
                    syncUserWithRevenueCat(currentUser)

                    val state = SessionState.Authenticated(currentUser)
                    _sessionState.value = state
                    return state
                } else {
                    // println("SessionManager: Token invalid, attempting refresh...")

                    // Try to refresh the token
                    val refreshedToken = authManager.getIdToken(forceRefresh = true)

                    if (refreshedToken != null) {
                        // println("SessionManager: Token refreshed successfully")
                        saveSessionToPreferences(currentUser, refreshedToken)

                        // Sync user with RevenueCat for subscription tracking
                        syncUserWithRevenueCat(currentUser)

                        val state = SessionState.Authenticated(currentUser)
                        _sessionState.value = state
                        return state
                    }
                }
            }

            // If remember me is set but Firebase session not ready, wait a bit
            val rememberMe = preferencesManager.getRememberMe()
            val savedUserId = preferencesManager.getUserId()

            if (rememberMe && savedUserId != null && retryCount < maxRetries - 1) {
                // println("SessionManager: RememberMe set, waiting for Firebase to restore session... (attempt ${retryCount + 1})")
                delay(500) // Wait for Firebase to initialize
                retryCount++
            } else {
                break
            }
        }

        // No valid session found
        // println("SessionManager: No valid session found, user needs to sign in")
        val state = SessionState.Unauthenticated
        _sessionState.value = state
        return state
    }

    /**
     * Save session data after successful sign-in.
     * Called by ViewModels after successful authentication.
     */
    suspend fun saveSession(user: User) {
        // println("SessionManager: Saving session for user: ${user.email}")

        val token = authManager.getIdToken(forceRefresh = false)
        saveSessionToPreferences(user, token)

        // Sync user with RevenueCat for subscription tracking
        syncUserWithRevenueCat(user)

        _sessionState.value = SessionState.Authenticated(user)
    }

    /**
     * Clear session data on sign-out.
     */
    fun clearSession() {
        // println("SessionManager: Clearing session")

        // Logout from RevenueCat to reset subscription state
        RevenueCatConfig.logOut()

        preferencesManager.clearAuthToken()
        preferencesManager.clearUserId()
        preferencesManager.saveRememberMe(false)

        _sessionState.value = SessionState.Unauthenticated
    }

    /**
     * Check if user has completed onboarding.
     */
    fun hasCompletedOnboarding(): Boolean {
        return preferencesManager.getHasCompletedOnboarding()
    }

    /**
     * Get the current user's email address.
     */
    fun getCurrentUserEmail(): String? {
        return preferencesManager.getUserEmail()
    }

    private fun saveSessionToPreferences(user: User, token: String?) {
        preferencesManager.saveUserId(user.uid)
        user.email?.let { preferencesManager.saveUserEmail(it) }
        user.displayName?.let { preferencesManager.saveUserName(it) }
        token?.let { preferencesManager.saveAuthToken(it) }
        preferencesManager.saveRememberMe(true)
    }

    /**
     * Sync user with RevenueCat for subscription tracking.
     * This ensures the user's purchases are associated with their account.
     * This is a suspending function that waits for RevenueCat login to complete.
     */
    private suspend fun syncUserWithRevenueCat(user: User) {
        // println("SessionManager: Syncing user with RevenueCat: ${user.uid}")
        try {
            val success = RevenueCatConfig.setUserId(user.uid)
            if (success) {
                // println("SessionManager: RevenueCat user sync completed successfully")
            } else {
                // println("SessionManager: RevenueCat user sync failed")
            }
        } catch (e: Exception) {
            // Don't fail the session for RevenueCat errors
            // println("SessionManager: Failed to sync with RevenueCat - ${e.message}")
        }
    }
}
