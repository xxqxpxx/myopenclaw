package com.myopenclaw.ui.screens.subscription

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.WorkspacePremium
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
import com.revenuecat.purchases.kmp.models.StoreProduct
import com.myopenclaw.ui.theme.*
import com.myopenclaw.ui.viewmodel.subscription.RevenueCatViewModel
import com.myopenclaw.ui.viewmodel.subscription.RevenueCatState
import com.myopenclaw.ui.viewmodel.subscription.PurchaseState
import org.koin.compose.viewmodel.koinViewModel

/**
 * Subscription Plans Screen using RevenueCat for real in-app purchases
 * Displays available subscription plans fetched from RevenueCat and handles native purchases
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionPlansScreenRevenueCat(
    onNavigateBack: () -> Unit,
    onPurchaseSuccess: () -> Unit = {},
    viewModel: RevenueCatViewModel = koinViewModel()
) {
    val revenueCatState by viewModel.state.collectAsState()
    val purchaseState by viewModel.purchaseState.collectAsState()
    val currentOffering by viewModel.currentOffering.collectAsState()
    val hasActiveSubscription by viewModel.hasActiveSubscription.collectAsState()

    // Extract packages from offering
    val packages = currentOffering?.availablePackages ?: emptyList()

    // Find specific packages - support various identifier formats
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

    // Selected package state
    var selectedPackageId by remember { mutableStateOf<String?>(monthlyPackage?.identifier ?: yearlyPackage?.identifier) }

    // Update selected package when offerings load
    LaunchedEffect(packages) {
        if (packages.isNotEmpty() && selectedPackageId == null) {
            // Default to monthly if available, otherwise yearly
            selectedPackageId = monthlyPackage?.identifier ?: yearlyPackage?.identifier ?: packages.firstOrNull()?.identifier
        }
        // println("SubscriptionPlansScreenRevenueCat: Available packages: ${packages.map { "${it.identifier}: ${it.storeProduct.price.formatted}" }}")
    }

    // Handle purchase success
    LaunchedEffect(purchaseState) {
        when (purchaseState) {
            is PurchaseState.Success -> {
                // println("SubscriptionPlansScreenRevenueCat: Purchase successful!")
                onPurchaseSuccess()
                viewModel.resetPurchaseState()
            }
            is PurchaseState.Error -> {
                // println("SubscriptionPlansScreenRevenueCat: Purchase error: ${(purchaseState as PurchaseState.Error).message}")
            }
            else -> {}
        }
    }

    // If already subscribed, show different UI
    LaunchedEffect(hasActiveSubscription) {
        if (hasActiveSubscription) {
            // println("SubscriptionPlansScreenRevenueCat: User already has active subscription")
        }
    }

    val isLoading = purchaseState is PurchaseState.Loading || revenueCatState is RevenueCatState.Loading

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Dark1
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top App Bar
            TopAppBar(
                title = {
                    Text(
                        "Choose Your Plan",
                        color = White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back",
                            tint = White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Dark1
                )
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Header
                Text(
                    text = "Unlock Premium Features",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Get access to exclusive market intelligence and AI-powered insights",
                    style = MaterialTheme.typography.bodyMedium,
                    color = White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (packages.isEmpty()) {
                    // No packages available
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Unable to load subscription plans",
                            style = MaterialTheme.typography.bodyLarge,
                            color = White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadOfferings() },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text("Retry")
                        }
                    }
                } else {
                    // Weekly Plan
                    weeklyPackage?.let { pkg ->
                        RevenueCatPlanCard(
                            package_ = pkg,
                            title = "Weekly Plan",
                            features = listOf(
                                "AI coding assistance",
                                "Basic code execution",
                                "Web search integration",
                                "Email support"
                            ),
                            isSelected = selectedPackageId == pkg.identifier,
                            isRecommended = false,
                            onClick = { selectedPackageId = pkg.identifier },
                            testTag = "subscription_weekly_plan_card"
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Monthly Plan (Recommended)
                    monthlyPackage?.let { pkg ->
                        RevenueCatPlanCard(
                            package_ = pkg,
                            title = "Monthly Plan",
                            features = listOf(
                                "All Weekly features",
                                "Advanced AI analysis",
                                "AI code assistant",
                                "Code execution sandbox",
                                "Priority support",
                                "File generation"
                            ),
                            isSelected = selectedPackageId == pkg.identifier,
                            isRecommended = true,
                            onClick = { selectedPackageId = pkg.identifier },
                            testTag = "subscription_monthly_plan_card"
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Yearly Plan
                    yearlyPackage?.let { pkg ->
                        RevenueCatPlanCard(
                            package_ = pkg,
                            title = "Yearly Plan",
                            features = listOf(
                                "All Monthly features",
                                "Unlimited AI analysis",
                                "1-on-1 strategy calls",
                                "Custom alerts",
                                "API access",
                                "Best value - Save 40%"
                            ),
                            isSelected = selectedPackageId == pkg.identifier,
                            isRecommended = false,
                            onClick = { selectedPackageId = pkg.identifier },
                            testTag = "subscription_yearly_plan_card"
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // If no standard packages found, show all available packages
                    if (weeklyPackage == null && monthlyPackage == null && yearlyPackage == null) {
                        packages.forEach { pkg ->
                            RevenueCatPlanCard(
                                package_ = pkg,
                                title = pkg.identifier.replace("_", " ").replace("$", "")
                                    .replaceFirstChar { it.uppercase() },
                                features = listOf(
                                    "Full access to all features",
                                    "AI-powered insights",
                                    "Real-time alerts",
                                    "Priority support"
                                ),
                                isSelected = selectedPackageId == pkg.identifier,
                                isRecommended = pkg == packages.first(),
                                onClick = { selectedPackageId = pkg.identifier }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Subscribe Button
                val selectedPackage = packages.firstOrNull { it.identifier == selectedPackageId }
                Button(
                    onClick = {
                        selectedPackage?.let { pkg ->
                            // println("SubscriptionPlansScreenRevenueCat: Initiating purchase for ${pkg.identifier}")
                            viewModel.purchase(pkg.storeProduct)
                        }
                    },
                    enabled = !isLoading && selectedPackage != null && !hasActiveSubscription,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("subscription_subscribe_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green2,
                        disabledContainerColor = Green2.copy(alpha = 0.5f)
                    ),
                    shape = CircleShape
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = White,
                            strokeWidth = 2.dp
                        )
                    } else if (hasActiveSubscription) {
                        Text(
                            text = "Already Subscribed",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = White
                        )
                    } else {
                        Text(
                            text = "Subscribe Now",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = White
                        )
                    }
                }

                // Error message
                val errorMessage = when {
                    purchaseState is PurchaseState.Error -> (purchaseState as PurchaseState.Error).message
                    revenueCatState is RevenueCatState.Error -> (revenueCatState as RevenueCatState.Error).message
                    else -> null
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = BearishRed,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Restore Purchases
                TextButton(
                    onClick = { viewModel.restorePurchases() },
                    enabled = !isLoading,
                    modifier = Modifier.testTag("subscription_restore_button")
                ) {
                    Text(
                        "Restore Purchases",
                        style = MaterialTheme.typography.bodyMedium,
                        color = White.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Footer
                Text(
                    text = "Cancel anytime. Subscriptions automatically renew unless cancelled at least 24 hours before the end of the current period.",
                    style = MaterialTheme.typography.bodySmall,
                    color = White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * Plan card component for displaying RevenueCat packages
 */
@Composable
private fun RevenueCatPlanCard(
    package_: Package,
    title: String,
    features: List<String>,
    isSelected: Boolean,
    isRecommended: Boolean,
    onClick: () -> Unit,
    testTag: String? = null
) {
    val product = package_.storeProduct
    val price = product.price.formatted
    val period = when {
        package_.identifier.contains("weekly", ignoreCase = true) -> "week"
        package_.identifier.contains("monthly", ignoreCase = true) -> "month"
        package_.identifier.contains("annual", ignoreCase = true) ||
        package_.identifier.contains("yearly", ignoreCase = true) -> "year"
        else -> product.period?.let {
            when {
                it.value == 1 && it.unit.name.contains("WEEK", ignoreCase = true) -> "week"
                it.value == 1 && it.unit.name.contains("MONTH", ignoreCase = true) -> "month"
                it.value == 1 && it.unit.name.contains("YEAR", ignoreCase = true) -> "year"
                else -> it.unit.name.lowercase()
            }
        } ?: "period"
    }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .then(testTag?.let { Modifier.testTag(it) } ?: Modifier),
        color = InputBackground,
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected || isRecommended) {
            BorderStroke(2.dp, if (isRecommended) PremiumGold else Primary)
        } else {
            null
        }
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isRecommended) Green2 else White
                    )

                    if (isRecommended) {
                        Surface(
                            color = PremiumGold,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "RECOMMENDED",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Dark1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Price
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = price,
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 36.sp),
                        fontWeight = FontWeight.Bold,
                        color = if (isRecommended) Green2 else White
                    )
                    Text(
                        text = "/$period",
                        style = MaterialTheme.typography.bodyLarge,
                        color = White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(
                    color = White.copy(alpha = 0.2f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Features
                features.forEach { feature ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = if (isRecommended) PremiumGold else Green2,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodyMedium,
                            color = White
                        )
                    }
                }
            }

            // Selection indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(24.dp)
                        .background(
                            color = Primary,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
