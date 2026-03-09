package com.myopenclaw.domain.repository

import com.myopenclaw.domain.models.CheckoutSession
import com.myopenclaw.domain.models.SubscriptionResponse

interface SubscriptionRepository {
    suspend fun createCheckoutSession(priceId: String): Result<CheckoutSession>
    suspend fun getSubscription(): Result<SubscriptionResponse>
    suspend fun cancelSubscription(reason: String?, feedback: String?): Result<Boolean>
    suspend fun reactivateSubscription(): Result<Boolean>
}
