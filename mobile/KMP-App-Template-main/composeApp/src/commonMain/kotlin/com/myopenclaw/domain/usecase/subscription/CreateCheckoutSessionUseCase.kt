package com.myopenclaw.domain.usecase.subscription

import com.myopenclaw.domain.models.CheckoutSession
import com.myopenclaw.domain.repository.SubscriptionRepository

class CreateCheckoutSessionUseCase(
    private val subscriptionRepository: SubscriptionRepository
) {
    suspend operator fun invoke(priceId: String): Result<CheckoutSession> {
        if (priceId.isBlank()) {
            return Result.failure(Exception("Price ID is required"))
        }

        return subscriptionRepository.createCheckoutSession(priceId)
    }
}
