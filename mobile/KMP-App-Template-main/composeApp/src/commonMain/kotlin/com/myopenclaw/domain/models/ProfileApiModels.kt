package com.myopenclaw.domain.models

import kotlinx.serialization.Serializable

/**
 * API Request/Response models for User Profile endpoints
 */

// ==================== Get Profile Response ====================
@Serializable
data class UserProfileResponse(
    val success: Boolean,
    val data: UserProfileData,
    val message: String? = null
)

@Serializable
data class UserProfileData(
    val id: String,
    val email: String,
    val fullName: String,
    val joinedDate: String, // ISO date string
    val subscription: SubscriptionInfoData,
    val preferences: UserPreferencesData
)

@Serializable
data class SubscriptionInfoData(
    val type: String, // "FREE", "PREMIUM", "TRIAL"
    val status: String, // "ACTIVE", "TRIAL", "EXPIRED", "CANCELLED"
    val planType: String?, // "WEEKLY", "MONTHLY", "YEARLY"
    val billingCycle: String?, // "WEEKLY", "MONTHLY", "YEARLY"
    val nextBillingDate: String?, // ISO date string
    val trialEndsIn: Int? = null, // Days remaining
    val renewalFrequency: String? = null // "Every week", "Every month", etc.
)

@Serializable
data class UserPreferencesData(
    val language: String = "English",
    val notificationsEnabled: Boolean = true,
    val marketAlertsEnabled: Boolean = true,
    val theme: String = "dark"
)

// ==================== Onboarding Data ====================
@Serializable
data class OnboardingDataRequest(
    val traderType: String? = null,
    val tradedAssets: String? = null,
    val biggestChallenge: String? = null,
    val confidenceLevel: String? = null,
    val analysisTime: String? = null,
    val chartMindset: String? = null,
    val desiredHelp: String? = null,
    val completedAt: String? = null // ISO date string when onboarding was completed
)

// ==================== Update Profile Request ====================
@Serializable
data class UpdateProfileRequest(
    val fullName: String? = null,
    val preferences: UserPreferencesData? = null,
    val onboarding: OnboardingDataRequest? = null
)

@Serializable
data class UpdateProfileResponse(
    val success: Boolean,
    val data: UserProfileData? = null,
    val message: String? = null
)

// ==================== Subscription Pricing Response ====================
@Serializable
data class SubscriptionPricingResponse(
    val success: Boolean,
    val data: List<SubscriptionPricingData>,
    val message: String? = null
)

@Serializable
data class SubscriptionPricingData(
    val planType: String, // "WEEKLY", "MONTHLY", "YEARLY"
    val price: Double,
    val currency: String = "USD",
    val billingFrequency: String, // "week", "month", "year"
    val trialDays: Int? = null,
    val discount: Int? = null, // Percentage
    val originalPrice: Double? = null,
    val priceId: String, // Stripe price ID
    val features: List<PremiumFeatureData>
)

@Serializable
data class PremiumFeatureData(
    val title: String,
    val description: String,
    val isIncluded: Boolean = true
)

// ==================== Delete Account Request ====================
@Serializable
data class DeleteAccountRequest(
    val reason: String? = null,
    val feedback: String? = null,
    val confirmEmail: String
)

@Serializable
data class DeleteAccountResponse(
    val success: Boolean,
    val message: String
)

// ==================== Extension Functions for Mapping ====================

/**
 * Convert API response data to domain model
 */
fun UserProfileData.toUserProfile(): UserProfile {
    return UserProfile(
        id = this.id,
        fullName = this.fullName,
        email = this.email,
        joinedDate = this.joinedDate,
        subscription = this.subscription.toSubscriptionInfo(),
        preferences = this.preferences.toUserPreferences()
    )
}

fun SubscriptionInfoData.toSubscriptionInfo(): SubscriptionInfo {
    return SubscriptionInfo(
        type = when (this.type) {
            "FREE" -> SubscriptionType.FREE
            "PREMIUM" -> SubscriptionType.PREMIUM
            "TRIAL" -> SubscriptionType.TRIAL
            else -> SubscriptionType.FREE
        },
        status = when (this.status) {
            "ACTIVE" -> SubscriptionStatus.ACTIVE
            "TRIAL" -> SubscriptionStatus.TRIAL
            "EXPIRED" -> SubscriptionStatus.EXPIRED
            "CANCELLED" -> SubscriptionStatus.CANCELLED
            "PENDING" -> SubscriptionStatus.PENDING
            else -> SubscriptionStatus.ACTIVE
        },
        planType = this.planType?.let {
            when (it) {
                "WEEKLY" -> PlanType.WEEKLY
                "MONTHLY" -> PlanType.MONTHLY
                "YEARLY" -> PlanType.YEARLY
                else -> null
            }
        },
        billingCycle = this.billingCycle?.let {
            when (it) {
                "WEEKLY" -> BillingCycle.WEEKLY
                "MONTHLY" -> BillingCycle.MONTHLY
                "YEARLY" -> BillingCycle.YEARLY
                else -> null
            }
        },
        nextBillingDate = this.nextBillingDate,
        trialEndsIn = this.trialEndsIn,
        renewalFrequency = this.renewalFrequency
    )
}

fun UserPreferencesData.toUserPreferences(): UserPreferences {
    return UserPreferences(
        language = this.language,
        notificationsEnabled = this.notificationsEnabled,
        marketAlertsEnabled = this.marketAlertsEnabled,
        theme = this.theme
    )
}

fun SubscriptionPricingData.toSubscriptionPricing(): SubscriptionPricing {
    return SubscriptionPricing(
        planType = when (this.planType) {
            "WEEKLY" -> PlanType.WEEKLY
            "MONTHLY" -> PlanType.MONTHLY
            "YEARLY" -> PlanType.YEARLY
            else -> PlanType.MONTHLY
        },
        price = this.price,
        currency = this.currency,
        billingFrequency = this.billingFrequency,
        trialDays = this.trialDays,
        discount = this.discount,
        originalPrice = this.originalPrice,
        features = this.features.map { it.toPremiumFeature() }
    )
}

fun PremiumFeatureData.toPremiumFeature(): PremiumFeature {
    return PremiumFeature(
        title = this.title,
        description = this.description,
        isIncluded = this.isIncluded
    )
}

/**
 * Convert domain model to API request data
 */
fun UserPreferences.toUserPreferencesData(): UserPreferencesData {
    return UserPreferencesData(
        language = this.language,
        notificationsEnabled = this.notificationsEnabled,
        marketAlertsEnabled = this.marketAlertsEnabled,
        theme = this.theme
    )
}
