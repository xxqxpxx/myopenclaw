package com.myopenclaw.ui.screens.subscription

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.revenuecat.purchases.kmp.models.Package
import com.myopenclaw.ui.theme.*
import com.myopenclaw.ui.viewmodel.subscription.RevenueCatViewModel
import com.myopenclaw.ui.viewmodel.subscription.RevenueCatState
import com.myopenclaw.ui.viewmodel.subscription.PurchaseState
import com.myopenclaw.util.UrlLauncher
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.compose.koinInject

/**
 * Subscription Required Screen - Hard paywall for returning non-subscribed users.
 * Always shows plan cards (with fallback prices) so the UI is never stuck on a loading spinner.
 */
@Composable
fun SubscriptionRequiredScreen(
    revenueCatViewModel: RevenueCatViewModel,
    onSubscriptionSuccess: () -> Unit,
    onSkipToHome: () -> Unit = {},
    onSignOut: () -> Unit
) {
    var selectedPlan by remember { mutableStateOf("yearly") }
    val scrollState = rememberScrollState()

    // Observe RevenueCat state
    val revenueCatState by revenueCatViewModel.state.collectAsState()
    val purchaseState by revenueCatViewModel.purchaseState.collectAsState()
    val currentOffering by revenueCatViewModel.currentOffering.collectAsState()

    // Extract packages from offering
    val packages = currentOffering?.availablePackages ?: emptyList()

    // Find specific packages by identifier
    val weeklyPackage = packages.firstOrNull {
        it.identifier.contains("weekly", ignoreCase = true) ||
        it.identifier == "\$rc_weekly"
    }
    val yearlyPackage = packages.firstOrNull {
        it.identifier.contains("annual", ignoreCase = true) ||
        it.identifier.contains("yearly", ignoreCase = true) ||
        it.identifier == "\$rc_annual"
    }

    // Get real prices from RevenueCat (with fallback display prices)
    val weeklyPrice = weeklyPackage?.storeProduct?.price?.formatted ?: "$4.99"
    val yearlyPrice = yearlyPackage?.storeProduct?.price?.formatted ?: "$99.99"

    // Handle purchase success
    LaunchedEffect(purchaseState) {
        when (purchaseState) {
            is PurchaseState.Success -> {
                // println("SubscriptionRequiredScreen: Purchase/Restore successful!")
                onSubscriptionSuccess()
                revenueCatViewModel.resetPurchaseState()
            }
            is PurchaseState.Error -> {
                // println("SubscriptionRequiredScreen: Purchase error: ${(purchaseState as PurchaseState.Error).message}")
            }
            else -> {}
        }
    }

    // Note: offerings are loaded in RevenueCatViewModel.init()
    // Avoid duplicate loadOfferings() calls which cause iPad freezes

    val isPurchasing = purchaseState is PurchaseState.Loading
    val isOffersLoading = revenueCatState is RevenueCatState.Loading
    val canPurchase = packages.isNotEmpty()

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

                // Lock Icon
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        color = Primary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Title
                Text(
                    text = "Subscription Required",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Subtitle
                Text(
                    text = "Subscribe to unlock all features and continue using myOpenClaw",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Features list
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FeatureItem(
                        icon = Icons.Default.Code,
                        text = "AI-powered code generation"
                    )
                    FeatureItem(
                        icon = Icons.Default.Terminal,
                        text = "Secure code execution sandbox"
                    )
                    FeatureItem(
                        icon = Icons.Default.Chat,
                        text = "Unlimited AI coding assistant"
                    )
                    FeatureItem(
                        icon = Icons.Default.Search,
                        text = "Web search and file generation"
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ALWAYS show plan cards - use fallback prices when offerings haven't loaded yet
                // Weekly Option
                Surface(
                    onClick = { selectedPlan = "weekly" },
                    modifier = Modifier.fillMaxWidth(),
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "$weeklyPrice per week",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            if (isOffersLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    color = Primary,
                                    strokeWidth = 1.5.dp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Yearly Option (Best Deal)
                Surface(
                    onClick = { selectedPlan = "yearly" },
                    modifier = Modifier.fillMaxWidth(),
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

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "$yearlyPrice per year",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                if (isOffersLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        color = Primary,
                                        strokeWidth = 1.5.dp
                                    )
                                }
                            }
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

                // Error message inline
                if (revenueCatState is RevenueCatState.Error && packages.isEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Unable to connect to subscription service. You can retry or continue without subscribing.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Bottom section - fixed, always visible
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                val selectedPackage: Package? = when (selectedPlan) {
                    "weekly" -> weeklyPackage
                    "yearly" -> yearlyPackage
                    else -> yearlyPackage
                }

                // Subscribe Button - or Retry button when offerings failed to load
                Button(
                    onClick = {
                        if (!canPurchase) {
                            // Retry loading offerings
                            revenueCatViewModel.loadOfferings()
                        } else if (selectedPackage != null) {
                            // println("SubscriptionRequiredScreen: Initiating purchase for ${selectedPackage.identifier}")
                            revenueCatViewModel.purchase(selectedPackage.storeProduct)
                        } else if (packages.isNotEmpty()) {
                            val fallbackPackage = packages.first()
                            revenueCatViewModel.purchase(fallbackPackage.storeProduct)
                        }
                    },
                    enabled = !isPurchasing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canPurchase) Green2 else Green2.copy(alpha = 0.8f),
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
                        Text(
                            text = if (canPurchase) "Subscribe Now" else "Retry Loading Plans",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                // Show error if purchase fails
                val errorMessage = when {
                    purchaseState is PurchaseState.Error -> (purchaseState as PurchaseState.Error).message
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

                Spacer(modifier = Modifier.height(8.dp))

                // Skip button - Always enabled so user is NEVER stuck
                OutlinedButton(
                    onClick = {
                        // println("SubscriptionRequiredScreen: User selected 'Maybe Later' - allowing preview")
                        onSkipToHome()
                    },
                    enabled = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
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

                Spacer(modifier = Modifier.height(8.dp))

                // Restore purchases & Sign out
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(
                        onClick = { revenueCatViewModel.restorePurchases() },
                        enabled = !isPurchasing
                    ) {
                        Text(
                            "Restore Purchases",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    TextButton(
                        onClick = {
                            // println("SubscriptionRequiredScreen: User signing out")
                            onSignOut()
                        }
                    ) {
                        Text(
                            "Sign Out",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
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
private fun FeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White
        )
    }
}
