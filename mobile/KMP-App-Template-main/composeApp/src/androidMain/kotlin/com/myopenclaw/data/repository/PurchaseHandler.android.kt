package com.myopenclaw.data.repository

import android.app.Activity
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.StoreProduct
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Android implementation of PurchaseHandler.
 *
 * Requires an Activity context to present the purchase UI.
 * The Activity is managed via setCurrentActivity/clearCurrentActivity
 * which should be called from MainActivity's onResume/onPause lifecycle methods.
 */
class AndroidPurchaseHandler(private val activity: Activity) : PurchaseHandler {
    override suspend fun purchase(product: StoreProduct): Result<CustomerInfo> =
        suspendCoroutine { continuation ->
            try {
                Purchases.sharedInstance.purchase(
                    storeProduct = product,
                    onError = { error, userCancelled ->
                        // // println("RevenueCat purchase error: ${error.message} (userCancelled: $userCancelled)")
                        continuation.resume(Result.failure(Exception(error.message)))
                    },
                    onSuccess = { _, customerInfo ->
                        // // println("RevenueCat purchase successful")
                        continuation.resume(Result.success(customerInfo))
                    }
                )
            } catch (e: Exception) {
                // // println("RevenueCat purchase exception: ${e.message}")
                continuation.resume(Result.failure(e))
            }
        }
}

/**
 * Global reference to the current Activity.
 * This should be managed by MainActivity's lifecycle callbacks.
 */
private var currentActivity: Activity? = null

/**
 * Sets the current activity for RevenueCat purchases.
 * Should be called from MainActivity.onResume()
 */
fun setCurrentActivity(activity: Activity) {
    currentActivity = activity
    // // println("RevenueCat: Activity set for purchases")
}

/**
 * Clears the current activity reference.
 * Should be called from MainActivity.onPause()
 */
fun clearCurrentActivity() {
    currentActivity = null
    // // println("RevenueCat: Activity reference cleared")
}

/**
 * Provides the Android-specific PurchaseHandler implementation.
 *
 * @throws IllegalStateException if Activity is not set via setCurrentActivity()
 */
actual fun getPurchaseHandler(): PurchaseHandler {
    val activity = currentActivity
        ?: throw IllegalStateException(
            "Activity not set. Call setCurrentActivity() from MainActivity.onResume() first."
        )
    return AndroidPurchaseHandler(activity)
}
