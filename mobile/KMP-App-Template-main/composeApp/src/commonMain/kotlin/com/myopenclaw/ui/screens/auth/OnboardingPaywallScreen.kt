package com.myopenclaw.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.revenuecat.purchases.kmp.models.Package
import com.myopenclaw.ui.theme.*
import com.myopenclaw.ui.viewmodel.subscription.CheckoutState
import com.myopenclaw.ui.viewmodel.subscription.SubscriptionViewModel
import com.myopenclaw.ui.viewmodel.subscription.RevenueCatViewModel
import com.myopenclaw.ui.viewmodel.subscription.RevenueCatState
import com.myopenclaw.ui.viewmodel.subscription.PurchaseState
import com.myopenclaw.util.UrlLauncher
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.compose.koinInject

enum class PaywallVariant {
    FREE_TRIAL_EMPHASIS,    // 3 days free trial
    DISCOUNT_EMPHASIS        // 80% discount in first year (HARD PAYWALL)
}

@Composable
fun OnboardingPaywallScreen(
    userName: String = "there",
    variant: PaywallVariant = PaywallVariant.DISCOUNT_EMPHASIS, // Hard paywall by default
    subscriptionViewModel: SubscriptionViewModel? = null,
    revenueCatViewModel: RevenueCatViewModel? = null,
    onContinue: (selectedPlan: String, wantFreeTrial: Boolean) -> Unit = { _, _ -> },
    onSkip: () -> Unit = {},
    onCheckoutUrlReady: (String) -> Unit = {},
    onPurchaseSuccess: () -> Unit = {}
) {
    // For hard paywall, yearly is always selected - no free trial option
    var selectedPlan by remember { mutableStateOf("yearly") }
    val scrollState = rememberScrollState()

    // Debug: Log ViewModels presence
    LaunchedEffect(Unit) {
        // println("OnboardingPaywallScreen: SubscriptionViewModel is ${if (subscriptionViewModel != null) "available" else "NULL"}")
        // println("OnboardingPaywallScreen: RevenueCatViewModel is ${if (revenueCatViewModel != null) "available" else "NULL"}")
    }

    // Observe RevenueCat state
    val revenueCatState by remember(revenueCatViewModel) {
        revenueCatViewModel?.state ?: MutableStateFlow(RevenueCatState.Idle)
    }.collectAsState()

    val purchaseState by remember(revenueCatViewModel) {
        revenueCatViewModel?.purchaseState ?: MutableStateFlow(PurchaseState.Idle)
    }.collectAsState()

    val currentOffering by remember(revenueCatViewModel) {
        revenueCatViewModel?.currentOffering ?: MutableStateFlow(null)
    }.collectAsState()

    // Extract packages from offering
    val packages = currentOffering?.availablePackages ?: emptyList()

    // Find specific packages by identifier
    val weeklyPackage = packages.firstOrNull {
        it.identifier.contains("weekly", ignoreCase = true) ||
        it.identifier == "\$rc_weekly"
    }
    val monthlyPackage = packages.firstOrNull {
        it.identifier.contains("monthly", ignoreCase = true) ||
        it.identifier == "\$rc_monthly"
    }
    val yearlyPackage = packages.firstOrNull {
        it.identifier.contains("annual", ignoreCase = true) ||
        it.identifier.contains("yearly", ignoreCase = true) ||
        it.identifier == "\$rc_annual"
    }

    // Get real prices from RevenueCat (with fallback display prices)
    val weeklyPrice = weeklyPackage?.storeProduct?.price?.formatted ?: "$9.99"
    val monthlyPrice = monthlyPackage?.storeProduct?.price?.formatted ?: "$29.99"
    val yearlyPrice = yearlyPackage?.storeProduct?.price?.formatted ?: "$79.99"

    // Trial state removed per Apple guideline 3.1.2(c) - toggles were confusing

    // Log available packages for debugging
    LaunchedEffect(packages) {
        // println("OnboardingPaywallScreen: Available packages: ${packages.map { "${it.identifier}: ${it.storeProduct.price.formatted}" }}")
    }

    // Handle purchase success
    LaunchedEffect(purchaseState) {
        when (purchaseState) {
            is PurchaseState.Success -> {
                // println("OnboardingPaywallScreen: Purchase successful!")
                onPurchaseSuccess()
                revenueCatViewModel?.resetPurchaseState()
            }
            is PurchaseState.Error -> {
                // println("OnboardingPaywallScreen: Purchase error: ${(purchaseState as PurchaseState.Error).message}")
            }
            else -> {}
        }
    }

    // Observe checkout state - only if SubscriptionViewModel is provided (legacy Stripe flow)
    val checkoutState by remember(subscriptionViewModel) {
        subscriptionViewModel?.checkoutState ?: MutableStateFlow(CheckoutState.Idle)
    }.collectAsState()

    val checkoutUrl by remember(subscriptionViewModel) {
        subscriptionViewModel?.checkoutUrl ?: MutableStateFlow<String?>(null)
    }.collectAsState()

    // Launch checkout URL when ready (legacy Stripe flow)
    LaunchedEffect(checkoutUrl) {
        checkoutUrl?.let { url ->
            // println("OnboardingPaywallScreen: Checkout URL ready: $url")
            onCheckoutUrlReady(url)
            subscriptionViewModel?.onCheckoutUrlOpened()
        }
    }

    // Only block UI for actual purchase operations, not for offerings loading
    val isPurchasing = purchaseState is PurchaseState.Loading || checkoutState is CheckoutState.Loading
    val isLoading = isPurchasing

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Dark1
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Scrollable content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                // Title - Hard paywall for yearly subscription
                Text(
                    text = "Try myopenclaw $userName\nGet 80% discount in first year",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Start,
                    lineHeight = 32.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Clear indication that subscription is required (Google Play compliance)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF2C3544),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "⚠️ Subscription required to access all features",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Timeline - Shows immediate access after payment
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    TimelineItem(
                        icon = Icons.Default.Person,
                        iconColor = Primary,
                        title = "Right now",
                        description = "Start coding with AI in seconds. Execute code in secure sandboxes. Get instant debugging help and code reviews.",
                        isActive = true
                    )

                    TimelineItem(
                        icon = Icons.Default.Notifications,
                        iconColor = Color(0xFF10B981), // Green
                        title = "in 369 days : Reminder",
                        description = "We will send you a reminder that your subscription is renewing soon",
                        isActive = false
                    )

                    TimelineItem(
                        icon = Icons.Default.AccountBalanceWallet,
                        iconColor = Color(0xFFFFA500), // Orange
                        title = "in 365 days : Billing starts",
                        description = "You will be charged for renewal, unless you cancel anytime before",
                        isActive = false
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Error info (inline, non-blocking)
                if (revenueCatState is RevenueCatState.Error && packages.isEmpty()) {
                    Text(
                        text = "Unable to connect to subscription service. You can retry or skip.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )
                }

                // ALWAYS show plan cards - prices update when offerings load
                // Monthly Option
                Surface(
                    onClick = { selectedPlan = "monthly" },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("paywall_monthly_plan_card"),
                    color = Color(0xFF1A2332),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        width = if (selectedPlan == "monthly") 2.dp else 0.dp,
                        color = if (selectedPlan == "monthly") Primary else Color.Transparent
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Monthly",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "$monthlyPrice/month",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Weekly Option
                Surface(
                    onClick = { selectedPlan = "weekly" },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("paywall_weekly_plan_card"),
                    color = Color(0xFF1A2332),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        width = if (selectedPlan == "weekly") 2.dp else 0.dp,
                        color = if (selectedPlan == "weekly") Primary else Color.Transparent
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Weekly",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "$weeklyPrice per week",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Yearly Access Option (Best Deal)
                Surface(
                    onClick = { selectedPlan = "yearly" },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("paywall_yearly_plan_card"),
                    color = Color(0xFF1A2332),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        width = if (selectedPlan == "yearly") 2.dp else 0.dp,
                        color = if (selectedPlan == "yearly") Primary else Color.Transparent
                    )
                ) {
                    Box {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Yearly",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                Surface(
                                    color = Primary,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "Save 80%",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "$yearlyPrice per year",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }

                        // BEST DEAL badge
                        Surface(
                            modifier = Modifier.align(Alignment.TopStart),
                            color = Primary,
                            shape = RoundedCornerShape(bottomEnd = 8.dp)
                        ) {
                            Text(
                                text = "BEST DEAL",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Cancel info
                Text(
                    text = "Cancel anytime",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // Add extra padding at bottom to ensure all options are scrollable
                Spacer(modifier = Modifier.height(120.dp))
            }

            // Continue Button (fixed at bottom, pill-shaped) - HARD PAYWALL
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                // Check if offerings are available for purchase
                val canPurchase = revenueCatViewModel != null && packages.isNotEmpty()
                val selectedPackage: Package? = when {
                    selectedPlan == "weekly" -> weeklyPackage
                    selectedPlan == "yearly" -> yearlyPackage
                    selectedPlan == "monthly" -> monthlyPackage
                    else -> yearlyPackage
                }
                
                Button(
                    onClick = {
                        // println("OnboardingPaywallScreen: Continue button clicked")
                        // println("OnboardingPaywallScreen: selectedPlan=$selectedPlan")

                        // HARD PAYWALL: Must use RevenueCat for real payment
                        if (!canPurchase) {
                            // Retry loading offerings
                            revenueCatViewModel?.loadOfferings()
                        } else if (revenueCatViewModel != null && selectedPackage != null) {
                            // println("OnboardingPaywallScreen: Initiating purchase for package: ${selectedPackage.identifier}")
                            // println("OnboardingPaywallScreen: Price: ${selectedPackage.storeProduct.price.formatted}")
                            // Initiate real payment through RevenueCat
                            revenueCatViewModel.purchase(selectedPackage.storeProduct)
                        } else if (revenueCatViewModel != null && packages.isNotEmpty()) {
                            // Try to find any available package
                            val fallbackPackage = packages.first()
                            // println("OnboardingPaywallScreen: Using fallback package: ${fallbackPackage.identifier}")
                            revenueCatViewModel.purchase(fallbackPackage.storeProduct)
                        } else {
                            // println("OnboardingPaywallScreen: ERROR - No packages available for purchase")
                            // println("OnboardingPaywallScreen: RevenueCat VM: ${revenueCatViewModel != null}, Packages: ${packages.size}")
                            // Show error - cannot proceed without payment in hard paywall mode
                        }
                    },
                    enabled = !isPurchasing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(bottom = 0.dp)
                        .testTag("paywall_subscribe_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green2,
                        disabledContainerColor = Green2.copy(alpha = 0.5f)
                    ),
                    shape = androidx.compose.foundation.shape.CircleShape
                ) {
                    if (isPurchasing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        val buttonText = when {
                            canPurchase -> "Continue"
                            else -> "Retry Loading Plans"
                        }
                        Text(
                            text = buttonText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                // Show error if purchase fails
                val errorMessage = when {
                    purchaseState is PurchaseState.Error -> (purchaseState as PurchaseState.Error).message
                    checkoutState is CheckoutState.Error -> (checkoutState as CheckoutState.Error).message
                    revenueCatState is RevenueCatState.Error -> (revenueCatState as RevenueCatState.Error).message
                    else -> null
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Dismiss/Skip button - Always enabled so user is never stuck
                OutlinedButton(
                    onClick = {
                        // println("OnboardingPaywallScreen: User selected 'Maybe Later' - allowing preview")
                        onSkip()
                    },
                    enabled = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("paywall_skip_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = BorderStroke(
                        width = 1.5.dp,
                        color = Color.White.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Maybe Later",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Restore purchases link
                TextButton(
                    onClick = {
                        revenueCatViewModel?.restorePurchases()
                    },
                    enabled = !isLoading && revenueCatViewModel != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Restore Purchases",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                // Terms of Use and Privacy Policy links (required by Apple 3.1.2)
                val urlLauncher: UrlLauncher = koinInject()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(onClick = { urlLauncher.openUrl("https://myopenclaw.com/terms") }) {
                        Text(
                            "Terms of Use",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                    Text(
                        " | ",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    TextButton(onClick = { urlLauncher.openUrl("https://myopenclaw.com/privacy") }) {
                        Text(
                            "Privacy Policy",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun TimelineItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    description: String,
    isActive: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            color = iconColor.copy(alpha = if (isActive) 1f else 0.3f),
            shape = RoundedCornerShape(50)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                lineHeight = 20.sp
            )
        }
    }
}
