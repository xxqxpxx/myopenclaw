package com.myopenclaw.data.remote

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.configure
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * RevenueCat API key - platform specific
 * This uses expect/actual pattern to provide the correct API key per platform
 */
expect val revenueCatApiKey: String

/**
 * RevenueCat Configuration
 * Handles SDK initialization for in-app purchases and subscriptions
 */
object RevenueCatConfig {
    private var isConfigured = false
    private var configurationError: String? = null

    /**
     * Initialize RevenueCat SDK with optional user ID and debug logging.
     *
     * @param userId Optional user ID for attribution (can be set later with setUserId)
     * @param enableDebugLogging Enable verbose logging for debugging (default: true)
     *
     * Should be called once at app startup (Application.onCreate on Android, app launch on iOS)
     */
    fun configure(userId: String? = null, enableDebugLogging: Boolean = true) {
        if (isConfigured) {
            // println("RevenueCatConfig: SDK already configured")
            return
        }

        try {
            // println("RevenueCatConfig: Configuring RevenueCat SDK...")

            Purchases.logLevel = if (enableDebugLogging) LogLevel.DEBUG else LogLevel.INFO

            Purchases.configure(apiKey = revenueCatApiKey) {
                if (userId != null) {
                    appUserId = userId
                    // println("RevenueCatConfig: Configured with user ID: $userId")
                }
            }

            isConfigured = true
            configurationError = null
            // println("RevenueCatConfig: SDK configured successfully")
        } catch (e: Exception) {
            val errorMessage = e.message ?: "Unknown error"
            // println("RevenueCatConfig: Error configuring SDK - $errorMessage")
            configurationError = errorMessage
            // Don't throw - allow app to continue without RevenueCat
            // Subscriptions will be unavailable but app will still work
            // println("RevenueCatConfig: App will continue without RevenueCat subscription features")
        }
    }

    /**
     * Set or update the user ID for attribution.
     * Call this after user authentication to link purchases to the user account.
     * This is a suspending function that waits for the login to complete.
     *
     * @param userId The user ID from your authentication system
     * @return true if login was successful, false otherwise
     */
    suspend fun setUserId(userId: String): Boolean {
        if (!isConfigured) {
            // println("RevenueCatConfig: SDK not configured. Call configure() first.")
            return false
        }

        return try {
            val result = withTimeoutOrNull(10_000L) {
                suspendCancellableCoroutine { continuation ->
                    try {
                        // println("RevenueCatConfig: Logging in user: $userId")
                        Purchases.sharedInstance.logIn(
                            newAppUserID = userId,
                            onError = { error ->
                                try {
                                    // println("RevenueCatConfig: Error logging in user - ${error.message}")
                                    if (continuation.isActive) {
                                        continuation.resume(false)
                                    }
                                } catch (e: Exception) {
                                    // println("RevenueCatConfig: Exception in onError callback - ${e.message}")
                                    if (continuation.isActive) {
                                        continuation.resume(false)
                                    }
                                }
                            },
                            onSuccess = { customerInfo, created ->
                                try {
                                    // println("RevenueCatConfig: User logged in successfully (created: $created)")
                                    val activeEntitlements = customerInfo.entitlements.active.keys
                                    // println("RevenueCatConfig: Active entitlements after login: $activeEntitlements")
                                    if (continuation.isActive) {
                                        continuation.resume(true)
                                    }
                                } catch (e: Exception) {
                                    // println("RevenueCatConfig: Exception in onSuccess callback - ${e.message}")
                                    if (continuation.isActive) {
                                        continuation.resume(false)
                                    }
                                }
                            }
                        )
                    } catch (e: Exception) {
                        // println("RevenueCatConfig: Exception while logging in user - ${e.message}")
                        if (continuation.isActive) {
                            continuation.resume(false)
                        }
                    }
                }
            }
            if (result == null) {
                // println("RevenueCatConfig: setUserId timed out after 10 seconds")
            }
            result ?: false
        } catch (e: Exception) {
            // println("RevenueCatConfig: Outer exception in setUserId - ${e.message}")
            false
        }
    }

    /**
     * Log out the current user and switch to an anonymous user.
     * Call this when the user signs out of your app.
     */
    fun logOut() {
        if (!isConfigured) {
            // println("RevenueCatConfig: SDK not configured")
            return
        }

        try {
            // println("RevenueCatConfig: Logging out user...")
            Purchases.sharedInstance.logOut(
                onError = { error ->
                    // println("RevenueCatConfig: Error logging out - ${error.message}")
                },
                onSuccess = { customerInfo ->
                    // println("RevenueCatConfig: User logged out successfully")
                }
            )
        } catch (e: Exception) {
            // println("RevenueCatConfig: Exception while logging out - ${e.message}")
            // Don't throw - allow app to continue
        }
    }

    /**
     * Check if RevenueCat has been configured successfully
     */
    fun isInitialized(): Boolean = isConfigured

    /**
     * Get the configuration error message, if any
     */
    fun getConfigurationError(): String? = configurationError

    /**
     * Check if there was a configuration error
     */
    fun hasConfigurationError(): Boolean = configurationError != null
}
