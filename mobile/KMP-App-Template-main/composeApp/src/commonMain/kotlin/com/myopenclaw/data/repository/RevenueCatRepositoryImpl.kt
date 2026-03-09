package com.myopenclaw.data.repository

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.Offering
import com.revenuecat.purchases.kmp.models.StoreProduct
import com.myopenclaw.data.local.PreferencesManager
import com.myopenclaw.data.remote.RevenueCatConfig
import com.myopenclaw.domain.repository.RevenueCatRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Implementation of RevenueCatRepository.
 *
 * Uses platform-specific PurchaseHandler for purchase operations.
 * Caches subscription status persistently to prevent redundant SDK calls that can
 * freeze the UI (especially on iPad where the main thread is more sensitive).
 */
class RevenueCatRepositoryImpl(
    private val preferencesManager: PreferencesManager
) : RevenueCatRepository {

    // In-memory cache backed by persistent storage
    private var cachedSubscriptionStatus: Boolean? = null
    private var subscriptionCacheTimestamp: Long = 0L
    private val subscriptionCacheMutex = Mutex()
    private companion object {
        const val SUBSCRIPTION_CACHE_TTL_MS = 300_000L // 5 minutes
    }

    init {
        // Restore cached subscription status from disk on startup
        cachedSubscriptionStatus = preferencesManager.getSubscriptionActive()
        subscriptionCacheTimestamp = preferencesManager.getSubscriptionCheckTimestamp()
    }

    /**
     * Check if RevenueCat SDK is properly configured and ready.
     * Returns an error result if not configured.
     */
    private fun <T> checkConfigured(): Result<T>? {
        if (!RevenueCatConfig.isInitialized()) {
            // println("RevenueCatRepository: SDK not initialized, skipping operation")
            return Result.failure(Exception("RevenueCat SDK not configured. Subscriptions are temporarily unavailable."))
        }
        return null
    }

    override suspend fun purchase(product: StoreProduct): Result<CustomerInfo> {
        checkConfigured<CustomerInfo>()?.let { return it }

        return try {
            // println("RevenueCatRepository: Initiating purchase for product: ${product.id}")
            val purchaseHandler = getPurchaseHandler()
            purchaseHandler.purchase(product)
        } catch (e: Exception) {
            // println("RevenueCatRepository: Purchase failed: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getOfferings(): Result<Offering?> {
        checkConfigured<Offering?>()?.let { return it }

        return try {
            // Add timeout to prevent permanent hang
            val result = withTimeoutOrNull(8_000L) {
                suspendCancellableCoroutine { continuation ->
                    try {
                        // println("RevenueCatRepository: Fetching offerings...")
                        Purchases.sharedInstance.getOfferings(
                            onError = { error ->
                                try {
                                    val errorMessage = error.message
                                    // println("RevenueCatRepository: Failed to fetch offerings: $errorMessage")
                                    if (continuation.isActive) {
                                        if (errorMessage?.contains("configuration", ignoreCase = true) == true) {
                                            // println("RevenueCatRepository: Configuration error detected, returning empty offerings")
                                            continuation.resume(Result.success(null))
                                        } else {
                                            continuation.resume(Result.failure(Exception(errorMessage)))
                                        }
                                    }
                                } catch (e: Exception) {
                                    // println("RevenueCatRepository: Exception in getOfferings onError callback - ${e.message}")
                                    if (continuation.isActive) {
                                        continuation.resume(Result.failure(e))
                                    }
                                }
                            },
                            onSuccess = { offerings ->
                                try {
                                    // println("RevenueCatRepository: Offerings fetched successfully")
                                    if (continuation.isActive) {
                                        continuation.resume(Result.success(offerings.current))
                                    }
                                } catch (e: Exception) {
                                    // println("RevenueCatRepository: Exception in getOfferings onSuccess callback - ${e.message}")
                                    if (continuation.isActive) {
                                        continuation.resume(Result.failure(e))
                                    }
                                }
                            }
                        )
                    } catch (e: Exception) {
                        val errorMessage = e.message ?: "Unknown error"
                        // println("RevenueCatRepository: Exception while fetching offerings: $errorMessage")
                        if (continuation.isActive) {
                            if (errorMessage.contains("configuration", ignoreCase = true) ||
                                errorMessage.contains("not configured", ignoreCase = true)) {
                                continuation.resume(Result.success(null))
                            } else {
                                continuation.resume(Result.failure(e))
                            }
                        }
                    }
                }
            }
            result ?: run {
                // println("RevenueCatRepository: getOfferings timed out after 15 seconds")
                Result.failure(Exception("Loading offerings timed out. Please check your internet connection and try again."))
            }
        } catch (e: Exception) {
            // println("RevenueCatRepository: Outer exception in getOfferings - ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getCustomerInfo(): Result<CustomerInfo> {
        checkConfigured<CustomerInfo>()?.let { return it }

        return try {
            val result = withTimeoutOrNull(5_000L) {
                suspendCancellableCoroutine { continuation ->
                    try {
                        // println("RevenueCatRepository: Fetching customer info...")
                        Purchases.sharedInstance.getCustomerInfo(
                            onError = { error ->
                                try {
                                    val errorMessage = error.message
                                    // println("RevenueCatRepository: Failed to fetch customer info: $errorMessage")
                                    if (continuation.isActive) {
                                        continuation.resume(Result.failure(Exception(errorMessage)))
                                    }
                                } catch (e: Exception) {
                                    // println("RevenueCatRepository: Exception in getCustomerInfo onError callback - ${e.message}")
                                    if (continuation.isActive) {
                                        continuation.resume(Result.failure(e))
                                    }
                                }
                            },
                            onSuccess = { customerInfo ->
                                try {
                                    // println("RevenueCatRepository: Customer info fetched successfully")
                                    if (continuation.isActive) {
                                        continuation.resume(Result.success(customerInfo))
                                    }
                                } catch (e: Exception) {
                                    // println("RevenueCatRepository: Exception in getCustomerInfo onSuccess callback - ${e.message}")
                                    if (continuation.isActive) {
                                        continuation.resume(Result.failure(e))
                                    }
                                }
                            }
                        )
                    } catch (e: Exception) {
                        // println("RevenueCatRepository: Exception while fetching customer info: ${e.message}")
                        if (continuation.isActive) {
                            continuation.resume(Result.failure(e))
                        }
                    }
                }
            }
            result ?: run {
                // println("RevenueCatRepository: getCustomerInfo timed out after 10 seconds")
                Result.failure(Exception("Fetching subscription info timed out."))
            }
        } catch (e: Exception) {
            // println("RevenueCatRepository: Outer exception in getCustomerInfo - ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun restorePurchases(): Result<CustomerInfo> {
        checkConfigured<CustomerInfo>()?.let { return it }

        return try {
            val result = withTimeoutOrNull(15_000L) {
                suspendCancellableCoroutine { continuation ->
                    try {
                        // println("RevenueCatRepository: Restoring purchases...")
                        Purchases.sharedInstance.restorePurchases(
                            onError = { error ->
                                try {
                                    // println("RevenueCatRepository: Failed to restore purchases: ${error.message}")
                                    if (continuation.isActive) {
                                        continuation.resume(Result.failure(Exception(error.message)))
                                    }
                                } catch (e: Exception) {
                                    // println("RevenueCatRepository: Exception in restorePurchases onError callback - ${e.message}")
                                    if (continuation.isActive) {
                                        continuation.resume(Result.failure(e))
                                    }
                                }
                            },
                            onSuccess = { customerInfo ->
                                try {
                                    // println("RevenueCatRepository: Purchases restored successfully")
                                    if (continuation.isActive) {
                                        continuation.resume(Result.success(customerInfo))
                                    }
                                } catch (e: Exception) {
                                    // println("RevenueCatRepository: Exception in restorePurchases onSuccess callback - ${e.message}")
                                    if (continuation.isActive) {
                                        continuation.resume(Result.failure(e))
                                    }
                                }
                            }
                        )
                    } catch (e: Exception) {
                        // println("RevenueCatRepository: Exception while restoring purchases: ${e.message}")
                        if (continuation.isActive) {
                            continuation.resume(Result.failure(e))
                        }
                    }
                }
            }
            result ?: run {
                // println("RevenueCatRepository: restorePurchases timed out after 15 seconds")
                Result.failure(Exception("Restoring purchases timed out. Please try again."))
            }
        } catch (e: Exception) {
            // println("RevenueCatRepository: Outer exception in restorePurchases - ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun hasActiveSubscription(): Boolean {
        if (!RevenueCatConfig.isInitialized()) {
            // println("RevenueCatRepository: SDK not initialized, returning false for subscription check")
            return false
        }

        // Return cached result if still fresh (prevents redundant SDK calls that freeze iPad)
        subscriptionCacheMutex.withLock {
            val now = currentTimeMillis()
            if (cachedSubscriptionStatus != null && (now - subscriptionCacheTimestamp) < SUBSCRIPTION_CACHE_TTL_MS) {
                // println("RevenueCatRepository: Returning cached subscription status: $cachedSubscriptionStatus")
                return cachedSubscriptionStatus!!
            }
        }

        return try {
            // Use a timeout to prevent blocking navigation on slow/failed SDK calls
            val hasSubscription = withTimeoutOrNull(5_000L) {
                try {
                    val result = getCustomerInfo()
                    val customerInfo = result.getOrNull()

                    // Check entitlements first (primary method)
                    val hasEntitlements = customerInfo?.entitlements?.active?.isNotEmpty() == true
                    // println("RevenueCatRepository: Active entitlements: ${customerInfo?.entitlements?.active?.keys ?: "none"}")

                    // Fallback: Check active subscriptions directly (in case entitlements aren't configured)
                    val hasActiveSubscriptions = customerInfo?.activeSubscriptions?.isNotEmpty() == true
                    // println("RevenueCatRepository: Active subscriptions: ${customerInfo?.activeSubscriptions ?: "none"}")

                    val hasSub = hasEntitlements || hasActiveSubscriptions
                    // println("RevenueCatRepository: Active subscription check: $hasSub (entitlements=$hasEntitlements, subscriptions=$hasActiveSubscriptions)")
                    hasSub
                } catch (e: Exception) {
                    // println("RevenueCatRepository: Exception in subscription check: ${e.message}")
                    false
                }
            }
            if (hasSubscription == null) {
                // println("RevenueCatRepository: hasActiveSubscription timed out after 10 seconds, returning false")
            }
            val result = hasSubscription ?: false

            // Cache the result in memory and on disk
            subscriptionCacheMutex.withLock {
                cachedSubscriptionStatus = result
                subscriptionCacheTimestamp = currentTimeMillis()
                preferencesManager.saveSubscriptionActive(result)
                preferencesManager.saveSubscriptionCheckTimestamp(subscriptionCacheTimestamp)
            }

            result
        } catch (e: Exception) {
            // println("RevenueCatRepository: Exception while checking subscription: ${e.message}")
            false
        }
    }

    /** Invalidate the subscription cache (call after purchase/restore) */
    fun invalidateSubscriptionCache() {
        cachedSubscriptionStatus = null
        subscriptionCacheTimestamp = 0L
        preferencesManager.saveSubscriptionCheckTimestamp(0L)
    }

    private fun currentTimeMillis(): Long {
        return kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
    }
}
