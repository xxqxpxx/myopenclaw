package com.myopenclaw.data.repository

import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.StoreProduct

/**
 * Platform-specific purchase handler interface.
 *
 * Implementations:
 * - Android: Requires Activity context for purchase flow
 * - iOS: Uses RevenueCat's internal UI handling
 */
interface PurchaseHandler {
    /**
     * Initiates a purchase for the given product.
     *
     * @param product The RevenueCat product to purchase
     * @return Result containing CustomerInfo on success or Exception on failure
     */
    suspend fun purchase(product: StoreProduct): Result<CustomerInfo>
}

/**
 * Provides platform-specific implementation of PurchaseHandler.
 *
 * Platform implementations:
 * - Android (PurchaseHandler.android.kt): Returns AndroidPurchaseHandler
 * - iOS (PurchaseHandler.ios.kt): Returns IOSPurchaseHandler
 */
expect fun getPurchaseHandler(): PurchaseHandler
