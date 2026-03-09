package com.myopenclaw.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class Subscription(
    val id: Int,
    val planId: String,
    val planName: String,
    val status: SubscriptionStatus,
    val currentPeriodStart: String,
    val currentPeriodEnd: String,
    val daysUntilRenewal: Int,
    val upcomingAmount: Int, // In cents
    val cancelAtPeriodEnd: Boolean
)

@Serializable
enum class SubscriptionStatus {
    ACTIVE,
    CANCELED,
    PAST_DUE,
    TRIALING,
    INCOMPLETE,
    INCOMPLETE_EXPIRED,
    UNPAID,
    // Additional statuses for user profile
    TRIAL,
    EXPIRED,
    CANCELLED,
    PENDING
}

@Serializable
data class SubscriptionResponse(
    val hasSubscription: Boolean,
    val subscription: Subscription? = null
)

@Serializable
data class CheckoutSession(
    val success: Boolean,
    val sessionId: String,
    val url: String
)

data class SubscriptionPlan(
    val id: String,
    val name: String,
    val price: Double,
    val billingPeriod: String,
    val priceId: String,
    val features: List<String>,
    val isRecommended: Boolean = false
)
