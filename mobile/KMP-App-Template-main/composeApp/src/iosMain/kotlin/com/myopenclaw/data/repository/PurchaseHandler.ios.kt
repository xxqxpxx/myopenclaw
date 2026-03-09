package com.myopenclaw.data.repository

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.StoreProduct
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * iOS implementation of PurchaseHandler.
 *
 * Unlike Android, iOS doesn't require a view controller to be passed.
 * RevenueCat handles the purchase UI internally on iOS.
 */
class IOSPurchaseHandler : PurchaseHandler {
    override suspend fun purchase(product: StoreProduct): Result<CustomerInfo> =
        try {
            suspendCancellableCoroutine { continuation ->
                try {
                    Purchases.sharedInstance.purchase(
                        storeProduct = product,
                        onError = { error, userCancelled ->
                            try {
                                // println("RevenueCat purchase error: ${error.message} (userCancelled: $userCancelled)")
                                if (continuation.isActive) {
                                    continuation.resume(Result.failure(Exception(error.message)))
                                }
                            } catch (e: Exception) {
                                // println("RevenueCat purchase onError callback exception: ${e.message}")
                            }
                        },
                        onSuccess = { _, customerInfo ->
                            try {
                                // println("RevenueCat purchase successful")
                                if (continuation.isActive) {
                                    continuation.resume(Result.success(customerInfo))
                                }
                            } catch (e: Exception) {
                                // println("RevenueCat purchase onSuccess callback exception: ${e.message}")
                            }
                        }
                    )
                } catch (e: Exception) {
                    // println("RevenueCat purchase exception: ${e.message}")
                    if (continuation.isActive) {
                        continuation.resume(Result.failure(e))
                    }
                }
            }
        } catch (e: Exception) {
            // println("RevenueCat purchase outer exception: ${e.message}")
            Result.failure(e)
        }
}

/**
 * Provides the iOS-specific PurchaseHandler implementation.
 *
 * No additional setup required - RevenueCat handles UI internally.
 */
actual fun getPurchaseHandler(): PurchaseHandler {
    return IOSPurchaseHandler()
}
