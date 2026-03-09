package com.myopenclaw.ui.viewmodel.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revenuecat.purchases.kmp.models.Offering
import com.revenuecat.purchases.kmp.models.StoreProduct
import com.myopenclaw.data.repository.RevenueCatRepositoryImpl
import com.myopenclaw.domain.repository.RevenueCatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

/**
 * UI State for RevenueCat purchases
 */
sealed class RevenueCatState {
    data object Idle : RevenueCatState()
    data object Loading : RevenueCatState()
    data class OfferingsLoaded(val offering: Offering) : RevenueCatState()
    data object PurchaseSuccess : RevenueCatState()
    data class Error(val message: String) : RevenueCatState()
}

/**
 * Purchase state for tracking in-progress purchases
 */
sealed class PurchaseState {
    data object Idle : PurchaseState()
    data object Loading : PurchaseState()
    data object Success : PurchaseState()
    data class Error(val message: String) : PurchaseState()
}

/**
 * ViewModel for handling RevenueCat in-app purchases
 *
 * Uses RevenueCatRepository to interact with the RevenueCat SDK
 * for native iOS/Android purchases.
 */
class RevenueCatViewModel(
    private val revenueCatRepository: RevenueCatRepository
) : ViewModel() {

    private val _state = MutableStateFlow<RevenueCatState>(RevenueCatState.Idle)
    val state: StateFlow<RevenueCatState> = _state.asStateFlow()

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()

    private val _currentOffering = MutableStateFlow<Offering?>(null)
    val currentOffering: StateFlow<Offering?> = _currentOffering.asStateFlow()

    private val _hasActiveSubscription = MutableStateFlow(false)
    val hasActiveSubscription: StateFlow<Boolean> = _hasActiveSubscription.asStateFlow()

    private var loadOfferingsJob: Job? = null

    companion object {
        private const val OFFERINGS_TIMEOUT_MS = 8_000L // 8 second timeout
    }

    init {
        // Load offerings first, then check subscription status sequentially
        // to avoid flooding the RevenueCat SDK with concurrent calls (causes iPad freeze)
        viewModelScope.launch {
            loadOfferingsInternal()
            checkSubscriptionStatusInternal()
        }
    }

    /**
     * Load available offerings from RevenueCat with a timeout
     * to prevent the UI from being stuck on "Loading plans..." forever.
     */
    fun loadOfferings() {
        // Cancel any in-flight loading to prevent duplicate requests
        loadOfferingsJob?.cancel()
        loadOfferingsJob = viewModelScope.launch {
            loadOfferingsInternal()
        }
    }

    /**
     * Internal implementation that can be called from a coroutine without launching a new one.
     */
    private suspend fun loadOfferingsInternal() {
        _state.value = RevenueCatState.Loading
        // println("RevenueCatViewModel: Loading offerings...")

        try {
            withTimeout(OFFERINGS_TIMEOUT_MS) {
                revenueCatRepository.getOfferings()
                    .onSuccess { offering ->
                        if (offering != null && offering.availablePackages.isNotEmpty()) {
                            _currentOffering.value = offering
                            _state.value = RevenueCatState.OfferingsLoaded(offering)
                            // println("RevenueCatViewModel: Offerings loaded successfully")
                            // println("RevenueCatViewModel: Available packages: ${offering.availablePackages.map { it.identifier }}")
                        } else {
                            // No offerings available - show error so user can retry
                            _state.value = RevenueCatState.Error("Subscription plans are currently unavailable. Please try again.")
                            // println("RevenueCatViewModel: No offerings available - subscription features disabled")
                        }
                    }
                    .onFailure { error ->
                        val errorMessage = error.message ?: "Failed to load offerings"
                        // Always show error state so user can retry - never leave in loading/idle
                        _state.value = RevenueCatState.Error(
                            if (errorMessage.contains("not configured", ignoreCase = true) ||
                                errorMessage.contains("configuration", ignoreCase = true) ||
                                errorMessage.contains("temporarily unavailable", ignoreCase = true)) {
                                "Subscription service is temporarily unavailable. Please try again later."
                            } else if (errorMessage.contains("timed out", ignoreCase = true)) {
                                "Loading plans timed out. Please check your connection and try again."
                            } else {
                                "Failed to load plans. Please try again."
                            }
                        )
                        // println("RevenueCatViewModel: Error loading offerings - $errorMessage")
                    }
            }
        } catch (e: TimeoutCancellationException) {
            // println("RevenueCatViewModel: Offerings loading timed out after ${OFFERINGS_TIMEOUT_MS}ms")
            _state.value = RevenueCatState.Error("Loading plans timed out. Please check your connection and try again.")
        } catch (e: Exception) {
            // println("RevenueCatViewModel: Exception in loadOfferings - ${e.message}")
            _state.value = RevenueCatState.Error("Failed to load plans. Please try again.")
        }
    }

    /**
     * Purchase a product
     */
    fun purchase(product: StoreProduct) {
        viewModelScope.launch {
            _purchaseState.value = PurchaseState.Loading
            // println("RevenueCatViewModel: Initiating purchase for ${product.id}")

            revenueCatRepository.purchase(product)
                .onSuccess { customerInfo ->
                    _purchaseState.value = PurchaseState.Success
                    _hasActiveSubscription.value = customerInfo.entitlements.active.isNotEmpty()
                    // Invalidate subscription cache after purchase
                    (revenueCatRepository as? RevenueCatRepositoryImpl)?.invalidateSubscriptionCache()
                    // println("RevenueCatViewModel: Purchase successful!")
                    // println("RevenueCatViewModel: Active entitlements: ${customerInfo.entitlements.active.keys}")
                }
                .onFailure { error ->
                    _purchaseState.value = PurchaseState.Error(error.message ?: "Purchase failed")
                    // println("RevenueCatViewModel: Purchase failed - ${error.message}")
                }
        }
    }

    /**
     * Restore previous purchases
     */
    fun restorePurchases() {
        viewModelScope.launch {
            _purchaseState.value = PurchaseState.Loading
            // println("RevenueCatViewModel: Restoring purchases...")

            revenueCatRepository.restorePurchases()
                .onSuccess { customerInfo ->
                    val hasActive = customerInfo.entitlements.active.isNotEmpty()
                    _hasActiveSubscription.value = hasActive
                    // Invalidate subscription cache after restore
                    (revenueCatRepository as? RevenueCatRepositoryImpl)?.invalidateSubscriptionCache()
                    _purchaseState.value = if (hasActive) {
                        PurchaseState.Success
                    } else {
                        PurchaseState.Error("No previous purchases found")
                    }
                    // println("RevenueCatViewModel: Restore completed. Active subscriptions: $hasActive")
                }
                .onFailure { error ->
                    _purchaseState.value = PurchaseState.Error(error.message ?: "Failed to restore purchases")
                    // println("RevenueCatViewModel: Restore failed - ${error.message}")
                }
        }
    }

    /**
     * Check current subscription status
     */
    fun checkSubscriptionStatus() {
        viewModelScope.launch {
            checkSubscriptionStatusInternal()
        }
    }

    /**
     * Internal implementation that can be called from a coroutine without launching a new one.
     */
    private suspend fun checkSubscriptionStatusInternal() {
        // println("RevenueCatViewModel: Checking subscription status...")
        val hasActive = revenueCatRepository.hasActiveSubscription()
        _hasActiveSubscription.value = hasActive
        // println("RevenueCatViewModel: Has active subscription: $hasActive")
    }

    /**
     * Reset purchase state (e.g., after showing success/error message)
     */
    fun resetPurchaseState() {
        _purchaseState.value = PurchaseState.Idle
    }

    /**
     * Get products from the current offering
     */
    fun getProducts(): List<StoreProduct> {
        return _currentOffering.value?.availablePackages?.map { it.storeProduct } ?: emptyList()
    }
}
