package com.myopenclaw.domain.usecase.subscription

import com.myopenclaw.domain.models.SubscriptionResponse
import com.myopenclaw.domain.repository.SubscriptionRepository

class GetSubscriptionUseCase(
    private val subscriptionRepository: SubscriptionRepository
) {
    suspend operator fun invoke(): Result<SubscriptionResponse> {
        return subscriptionRepository.getSubscription()
    }
}
