package com.myopenclaw.data.repository

import com.myopenclaw.data.auth.AuthManager
import com.myopenclaw.data.remote.ApiService
import com.myopenclaw.domain.models.CheckoutSession
import com.myopenclaw.domain.models.SubscriptionResponse
import com.myopenclaw.domain.repository.SubscriptionRepository

class SubscriptionRepositoryImpl(
    private val apiService: ApiService
) : SubscriptionRepository {

    override suspend fun createCheckoutSession(priceId: String): Result<CheckoutSession> {
        return try {
            val session = apiService.createCheckoutSession(priceId)
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSubscription(): Result<SubscriptionResponse> {
        return try {
            val response = apiService.getSubscription()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelSubscription(
        reason: String?,
        feedback: String?
    ): Result<Boolean> {
        return try {
            apiService.cancelSubscription(reason, feedback)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun reactivateSubscription(): Result<Boolean> {
        return try {
            apiService.reactivateSubscription()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
