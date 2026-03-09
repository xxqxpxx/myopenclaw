package com.myopenclaw.ui.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myopenclaw.data.local.PreferencesManager
import com.myopenclaw.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class OnboardingViewModel(
    private val preferencesManager: PreferencesManager,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow<OnboardingState>(OnboardingState.Idle)
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    private val _onboardingData = MutableStateFlow(OnboardingData())
    val onboardingData: StateFlow<OnboardingData> = _onboardingData.asStateFlow()

    /**
     * Save user name from email signup screen
     */
    fun saveUserName(name: String) {
        // println("OnboardingViewModel: saveUserName called with name='$name'")
        // Update state immediately for navigation
        _onboardingData.value = _onboardingData.value.copy(userName = name)
        // println("OnboardingViewModel: State updated, userName='${_onboardingData.value.userName}'")

        // Save to preferences asynchronously
        viewModelScope.launch {
            try {
                preferencesManager.saveUserName(name)
                // println("OnboardingViewModel: Saved userName to preferences")
            } catch (e: Exception) {
                // println("OnboardingViewModel: ERROR saving userName: ${e.message}")
                _state.value = OnboardingState.Error("Failed to save user name: ${e.message}")
            }
        }
    }

    /**
     * Save user email from email signup screen
     */
    fun saveUserEmail(email: String) {
        // println("OnboardingViewModel: saveUserEmail called with email='$email'")
        // Update state immediately for navigation
        _onboardingData.value = _onboardingData.value.copy(userEmail = email)
        // println("OnboardingViewModel: State updated, userEmail='${_onboardingData.value.userEmail}'")

        // Save to preferences asynchronously
        viewModelScope.launch {
            try {
                preferencesManager.saveUserEmail(email)
                // println("OnboardingViewModel: Saved userEmail to preferences")
            } catch (e: Exception) {
                // println("OnboardingViewModel: ERROR saving userEmail: ${e.message}")
                _state.value = OnboardingState.Error("Failed to save email: ${e.message}")
            }
        }
    }

    /**
     * Save all questionnaire answers locally and sync to backend
     */
    fun saveQuestionnaireAnswers(answers: OnboardingAnswers) {
        viewModelScope.launch {
            _state.value = OnboardingState.Saving
            try {
                // Save each answer to local preferences
                preferencesManager.saveBiggestChallenge(answers.biggestChallenge)
                preferencesManager.saveConfidenceLevel(answers.confidenceLevel)
                preferencesManager.saveAnalysisTime(answers.analysisTime)
                preferencesManager.saveChartMindset(answers.chartMindset)
                preferencesManager.saveDesiredHelp(answers.desiredHelp)
                preferencesManager.saveTraderType(answers.traderType)
                preferencesManager.saveTradedAssets(answers.tradedAssets)

                // Update local state
                _onboardingData.value = _onboardingData.value.copy(questionnaireAnswers = answers)

                // Sync to backend (fire and forget - don't block onboarding flow)
                syncOnboardingToBackend(answers)

                _state.value = OnboardingState.Idle
            } catch (e: Exception) {
                _state.value = OnboardingState.Error("Failed to save questionnaire answers: ${e.message}")
            }
        }
    }

    /**
     * Sync onboarding data to backend
     * This is called after saving locally to ensure data is persisted to server
     */
    private fun syncOnboardingToBackend(answers: OnboardingAnswers, isComplete: Boolean = false) {
        viewModelScope.launch {
            try {
                // println("OnboardingViewModel: Syncing onboarding data to backend...")
                val result = userRepository.syncOnboardingData(
                    traderType = answers.traderType.ifEmpty { null },
                    tradedAssets = answers.tradedAssets.ifEmpty { null },
                    biggestChallenge = answers.biggestChallenge.ifEmpty { null },
                    confidenceLevel = answers.confidenceLevel.ifEmpty { null },
                    analysisTime = answers.analysisTime.ifEmpty { null },
                    chartMindset = answers.chartMindset.ifEmpty { null },
                    desiredHelp = answers.desiredHelp.ifEmpty { null },
                    completedAt = if (isComplete) Clock.System.now().toString() else null
                )

                result.onSuccess {
                    // println("OnboardingViewModel: Onboarding data synced to backend successfully")
                }.onFailure { error ->
                    // Log but don't block the user - local data is already saved
                    // println("OnboardingViewModel: Failed to sync onboarding to backend: ${error.message}")
                }
            } catch (e: Exception) {
                // Log but don't block the user - local data is already saved
                // println("OnboardingViewModel: Exception syncing onboarding to backend: ${e.message}")
            }
        }
    }

    /**
     * Save subscription plan choice from paywall screen
     */
    fun saveSubscriptionChoice(plan: String, wantsFreeTrial: Boolean) {
        viewModelScope.launch {
            _state.value = OnboardingState.Saving
            try {
                preferencesManager.saveSelectedSubscriptionPlan(plan)
                preferencesManager.saveWantsFreeTrial(wantsFreeTrial)

                _onboardingData.value = _onboardingData.value.copy(
                    selectedSubscriptionPlan = plan,
                    wantsFreeTrial = wantsFreeTrial
                )
                _state.value = OnboardingState.Idle
            } catch (e: Exception) {
                _state.value = OnboardingState.Error("Failed to save subscription choice: ${e.message}")
            }
        }
    }

    /**
     * Mark onboarding as complete and sync to backend
     */
    fun completeOnboarding() {
        viewModelScope.launch {
            _state.value = OnboardingState.Saving
            try {
                preferencesManager.saveHasCompletedOnboarding(true)

                // Sync completion status to backend with all questionnaire answers
                val answers = _onboardingData.value.questionnaireAnswers
                syncOnboardingToBackend(answers, isComplete = true)

                _state.value = OnboardingState.Completed
            } catch (e: Exception) {
                _state.value = OnboardingState.Error("Failed to complete onboarding: ${e.message}")
            }
        }
    }

    /**
     * Load existing onboarding data from preferences
     */
    fun loadOnboardingData() {
        viewModelScope.launch {
            try {
                val userName = preferencesManager.getUserName() ?: ""
                val userEmail = preferencesManager.getUserEmail() ?: ""
                val biggestChallenge = preferencesManager.getBiggestChallenge() ?: ""
                val confidenceLevel = preferencesManager.getConfidenceLevel() ?: ""
                val analysisTime = preferencesManager.getAnalysisTime() ?: ""
                val chartMindset = preferencesManager.getChartMindset() ?: ""
                val desiredHelp = preferencesManager.getDesiredHelp() ?: ""
                val traderType = preferencesManager.getTraderType() ?: ""
                val tradedAssets = preferencesManager.getTradedAssets() ?: ""
                val selectedPlan = preferencesManager.getSelectedSubscriptionPlan() ?: ""
                val wantsFreeTrial = preferencesManager.getWantsFreeTrial()

                _onboardingData.value = OnboardingData(
                    userName = userName,
                    userEmail = userEmail,
                    questionnaireAnswers = OnboardingAnswers(
                        biggestChallenge = biggestChallenge,
                        confidenceLevel = confidenceLevel,
                        analysisTime = analysisTime,
                        chartMindset = chartMindset,
                        desiredHelp = desiredHelp,
                        traderType = traderType,
                        tradedAssets = tradedAssets
                    ),
                    selectedSubscriptionPlan = selectedPlan,
                    wantsFreeTrial = wantsFreeTrial
                )
            } catch (e: Exception) {
                _state.value = OnboardingState.Error("Failed to load onboarding data: ${e.message}")
            }
        }
    }

    /**
     * Check if user has completed onboarding
     */
    fun hasCompletedOnboarding(): Boolean {
        return preferencesManager.getHasCompletedOnboarding()
    }

    /**
     * Get user's trader type for personalization
     */
    fun getTraderType(): String? {
        return preferencesManager.getTraderType()
    }

    /**
     * Get user's traded assets for personalization
     */
    fun getTradedAssets(): String? {
        return preferencesManager.getTradedAssets()
    }

    /**
     * Reset onboarding state
     */
    fun resetState() {
        _state.value = OnboardingState.Idle
    }
}
