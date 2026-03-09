package com.myopenclaw.ui.viewmodel.auth

data class OnboardingAnswers(
    val biggestChallenge: String = "",
    val confidenceLevel: String = "",
    val analysisTime: String = "",
    val chartMindset: String = "",
    val desiredHelp: String = "",
    val traderType: String = "",
    val tradedAssets: String = ""
)

data class OnboardingData(
    val userName: String = "",
    val userEmail: String = "",
    val questionnaireAnswers: OnboardingAnswers = OnboardingAnswers(),
    val selectedSubscriptionPlan: String = "",
    val wantsFreeTrial: Boolean = false
)

sealed class OnboardingState {
    data object Idle : OnboardingState()
    data object Saving : OnboardingState()
    data object Completed : OnboardingState()
    data class Error(val message: String) : OnboardingState()
}
