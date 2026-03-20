package com.myopenclaw.domain.models

import kotlinx.serialization.Serializable

/**
 * User profile information
 */
@Serializable
data class UserProfile(
    val id: String,
    val fullName: String,
    val email: String,
    val joinedDate: String, // ISO date or formatted string
    val subscription: SubscriptionInfo,
    val preferences: UserPreferences = UserPreferences()
)

/**
 * Subscription information
 */
@Serializable
data class SubscriptionInfo(
    val type: SubscriptionType,
    val status: SubscriptionStatus,
    val planType: PlanType?,
    val billingCycle: BillingCycle?,
    val nextBillingDate: String?, // ISO date or formatted string
    val trialEndsIn: Int? = null, // Days remaining in trial
    val renewalFrequency: String? = null // "Every week", "Every month", etc.
)

/**
 * Subscription type
 */
@Serializable
enum class SubscriptionType {
    FREE,
    PREMIUM,
    TRIAL
}

/**
 * Plan types for profile screens
 */
@Serializable
enum class PlanType {
    WEEKLY,
    MONTHLY,
    YEARLY
}

/**
 * Billing cycle
 */
@Serializable
enum class BillingCycle {
    WEEKLY,
    MONTHLY,
    YEARLY
}

/**
 * User preferences
 */
@Serializable
data class UserPreferences(
    val language: String = "English",
    val notificationsEnabled: Boolean = true,
    val marketAlertsEnabled: Boolean = true,
    val theme: String = "dark"
)

/**
 * Subscription pricing information
 */
@Serializable
data class SubscriptionPricing(
    val planType: PlanType,
    val price: Double,
    val currency: String = "USD",
    val billingFrequency: String,
    val trialDays: Int? = null,
    val discount: Int? = null, // Percentage
    val originalPrice: Double? = null,
    val features: List<PremiumFeature>
)

/**
 * Premium feature description
 */
@Serializable
data class PremiumFeature(
    val title: String,
    val description: String,
    val isIncluded: Boolean = true
)

/**
 * Sample data for testing
 */
object SampleUserData {
    val premiumFeatures = listOf(
        PremiumFeature(
            title = "Unlimited AI Coding",
            description = "Write, debug, and optimize code in 50+ languages with Claude AI assistance."
        ),
        PremiumFeature(
            title = "Code Execution Sandbox",
            description = "Run code in secure cloud sandboxes directly from your conversations."
        ),
        PremiumFeature(
            title = "AI Code Reviews",
            description = "Get instant code reviews, refactoring suggestions, and best practice guidance."
        ),
        PremiumFeature(
            title = "File Generation",
            description = "Generate code files, CSVs, PDFs, and more — download or share instantly."
        )
    )

    val freeUser = UserProfile(
        id = "user_001",
        fullName = "Blake Maddison",
        email = "blakemaddisoncornrow@gmail.com",
        joinedDate = "Aug 24, 2025",
        subscription = SubscriptionInfo(
            type = SubscriptionType.FREE,
            status = SubscriptionStatus.ACTIVE,
            planType = null,
            billingCycle = null,
            nextBillingDate = null,
            trialEndsIn = 2
        ),
        preferences = UserPreferences(
            language = "English"
        )
    )

    val premiumUser = UserProfile(
        id = "user_002",
        fullName = "Blake Maddison",
        email = "blakemaddisoncornrow@gmail.com",
        joinedDate = "Aug 24, 2025",
        subscription = SubscriptionInfo(
            type = SubscriptionType.PREMIUM,
            status = SubscriptionStatus.ACTIVE,
            planType = PlanType.MONTHLY,
            billingCycle = BillingCycle.MONTHLY,
            nextBillingDate = "24th april 2025",
            renewalFrequency = "Every month"
        ),
        preferences = UserPreferences(
            language = "English"
        )
    )

    val weeklyPricing = SubscriptionPricing(
        planType = PlanType.WEEKLY,
        price = 6.99,
        billingFrequency = "week",
        trialDays = 3,
        features = premiumFeatures
    )

    val monthlyPricing = SubscriptionPricing(
        planType = PlanType.MONTHLY,
        price = 19.99,
        billingFrequency = "month",
        features = premiumFeatures
    )

    val yearlyPricing = SubscriptionPricing(
        planType = PlanType.YEARLY,
        price = 59.99,
        billingFrequency = "year",
        discount = 80,
        originalPrice = 399.99,
        features = premiumFeatures
    )
}
