package com.myopenclaw.ui.viewmodel.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myopenclaw.data.remote.ApiConfig
import com.myopenclaw.domain.models.CheckoutSession
import com.myopenclaw.domain.models.SubscriptionPlan
import com.myopenclaw.domain.models.SubscriptionResponse
import com.myopenclaw.domain.usecase.subscription.CreateCheckoutSessionUseCase
import com.myopenclaw.domain.usecase.subscription.GetSubscriptionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SubscriptionState {
    data object Idle : SubscriptionState()
    data object Loading : SubscriptionState()
    data class Success(val subscription: SubscriptionResponse) : SubscriptionState()
    data class Error(val message: String) : SubscriptionState()
}

sealed class CheckoutState {
    data object Idle : CheckoutState()
    data object Loading : CheckoutState()
    data class Success(val session: CheckoutSession) : CheckoutState()
    data class Error(val message: String) : CheckoutState()
}

class SubscriptionViewModel(
    private val getSubscriptionUseCase: GetSubscriptionUseCase,
    private val createCheckoutSessionUseCase: CreateCheckoutSessionUseCase
) : ViewModel() {

    private val _subscriptionState = MutableStateFlow<SubscriptionState>(SubscriptionState.Idle)
    val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState.asStateFlow()

    private val _checkoutState = MutableStateFlow<CheckoutState>(CheckoutState.Idle)
    val checkoutState: StateFlow<CheckoutState> = _checkoutState.asStateFlow()

    // Checkout URL for external browser redirect
    private val _checkoutUrl = MutableStateFlow<String?>(null)
    val checkoutUrl: StateFlow<String?> = _checkoutUrl.asStateFlow()

    /**
     * Available subscription plans with real Stripe price IDs from ApiConfig
     */
    val plans: List<SubscriptionPlan>
        get() = listOf(
            SubscriptionPlan(
                id = "weekly",
                name = "Weekly Plan",
                price = 29.99,
                billingPeriod = "week",
                priceId = ApiConfig.Stripe.PRICE_WEEKLY,
                features = listOf(
                    "Real-time insider trading alerts",
                    "Congressional trading insights",
                    "Basic AI trade ideas",
                    "Email support"
                )
            ),
            SubscriptionPlan(
                id = "monthly",
                name = "Monthly Plan",
                price = 100.0,
                billingPeriod = "month",
                priceId = ApiConfig.Stripe.PRICE_MONTHLY,
                features = listOf(
                    "All Weekly features",
                    "Advanced AI analysis",
                    "Chart analysis tool",
                    "Market impact predictions",
                    "Priority support",
                    "Options flow data"
                ),
                isRecommended = true
            ),
            SubscriptionPlan(
                id = "quarterly",
                name = "Quarterly Plan",
                price = 250.0,
                billingPeriod = "quarter",
                priceId = ApiConfig.Stripe.PRICE_QUARTERLY,
                features = listOf(
                    "All Monthly features",
                    "Unlimited AI analysis",
                    "1-on-1 strategy calls",
                    "Custom alerts",
                    "API access",
                    "17% discount"
                )
            )
        )

    fun loadSubscription() {
        viewModelScope.launch {
            _subscriptionState.value = SubscriptionState.Loading

            getSubscriptionUseCase()
                .onSuccess { subscription ->
                    _subscriptionState.value = SubscriptionState.Success(subscription)
                }
                .onFailure { exception ->
                    _subscriptionState.value = SubscriptionState.Error(
                        exception.message ?: "Failed to load subscription"
                    )
                }
        }
    }

    /**
     * Creates a Stripe checkout session for the given price ID
     * On success, the checkout URL is stored in checkoutUrl for the UI to open
     */
    fun createCheckout(priceId: String) {
        viewModelScope.launch {
            _checkoutState.value = CheckoutState.Loading
            _checkoutUrl.value = null

            createCheckoutSessionUseCase(priceId)
                .onSuccess { session ->
                    _checkoutState.value = CheckoutState.Success(session)
                    _checkoutUrl.value = session.url
                }
                .onFailure { exception ->
                    _checkoutState.value = CheckoutState.Error(
                        exception.message ?: "Failed to create checkout session"
                    )
                }
        }
    }

    /**
     * Called after the checkout URL has been opened
     */
    fun onCheckoutUrlOpened() {
        _checkoutUrl.value = null
    }

    fun resetCheckoutState() {
        _checkoutState.value = CheckoutState.Idle
        _checkoutUrl.value = null
    }
}
