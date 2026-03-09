package com.myopenclaw.domain.repository

import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.Offering
import com.revenuecat.purchases.kmp.models.StoreProduct

/**
 * Repository interface for RevenueCat subscription operations.
 *
 * Handles all subscription-related operations including purchases,
 * offerings retrieval, and subscription status checks.
 */
interface RevenueCatRepository {
    /**
     * Purchase a subscription product.
     *
     * @param product The product to purchase
     * @return Result containing CustomerInfo on success or Exception on failure
     */
    suspend fun purchase(product: StoreProduct): Result<CustomerInfo>

    /**
     * Get available subscription offerings.
     *
     * @return Result containing Offering or null if no offerings available
     */
    suspend fun getOfferings(): Result<Offering?>

    /**
     * Get current customer information.
     *
     * @return Result containing CustomerInfo
     */
    suspend fun getCustomerInfo(): Result<CustomerInfo>

    /**
     * Restore previous purchases.
     *
     * @return Result containing updated CustomerInfo
     */
    suspend fun restorePurchases(): Result<CustomerInfo>

    /**
     * Check if user has an active subscription.
     *
     * @return true if user has active subscription, false otherwise
     */
    suspend fun hasActiveSubscription(): Boolean
}
