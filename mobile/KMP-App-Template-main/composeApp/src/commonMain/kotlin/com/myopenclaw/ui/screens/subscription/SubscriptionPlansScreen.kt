package com.myopenclaw.ui.screens.subscription

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.myopenclaw.domain.models.SubscriptionPlan
import com.myopenclaw.ui.components.LoadingScreen
import com.myopenclaw.ui.components.SWPrimaryButton
import com.myopenclaw.ui.theme.Dimensions
import com.myopenclaw.ui.theme.PremiumGold
import com.myopenclaw.ui.viewmodel.subscription.CheckoutState
import com.myopenclaw.ui.viewmodel.subscription.SubscriptionViewModel
import com.myopenclaw.util.UrlLauncher
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionPlansScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: SubscriptionViewModel = koinViewModel(),
    urlLauncher: UrlLauncher = koinInject()
) {
    val checkoutState by viewModel.checkoutState.collectAsState()

    // Handle checkout state
    LaunchedEffect(checkoutState) {
        when (checkoutState) {
            is CheckoutState.Success -> {
                val session = (checkoutState as CheckoutState.Success).session
                // Open the Stripe checkout URL in browser
                urlLauncher.openUrlInApp(session.url)
                viewModel.resetCheckoutState()
            }
            else -> {}
        }
    }

    // Show error dialog
    if (checkoutState is CheckoutState.Error) {
        AlertDialog(
            onDismissRequest = { viewModel.resetCheckoutState() },
            title = { Text("Checkout Error") },
            text = { Text((checkoutState as CheckoutState.Error).message) },
            confirmButton = {
                TextButton(onClick = { viewModel.resetCheckoutState() }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose Your Plan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (checkoutState is CheckoutState.Loading) {
            LoadingScreen()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = Dimensions.paddingLarge)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(Dimensions.spacingMedium))

                // Header
                Text(
                    text = "Unlock Premium Features",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Dimensions.spacingSmall))

                Text(
                    text = "Get access to exclusive market intelligence and AI-powered insights",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Dimensions.spacingXLarge))

                // Plans
                viewModel.plans.forEach { plan ->
                    PlanCard(
                        plan = plan,
                        onSubscribe = {
                            viewModel.createCheckout(plan.priceId)
                        }
                    )
                    Spacer(modifier = Modifier.height(Dimensions.spacingMedium))
                }

                Spacer(modifier = Modifier.height(Dimensions.spacingLarge))

                // Footer
                Text(
                    text = "All plans include 7-day free trial. Cancel anytime.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = Dimensions.paddingMedium)
                )

                Spacer(modifier = Modifier.height(Dimensions.spacingXLarge))
            }
        }
    }
}

@Composable
fun PlanCard(
    plan: SubscriptionPlan,
    onSubscribe: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (plan.isRecommended) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = if (plan.isRecommended) {
            BorderStroke(2.dp, PremiumGold)
        } else {
            null
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingLarge)
        ) {
            // Header with badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = plan.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (plan.isRecommended) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                if (plan.isRecommended) {
                    Surface(
                        color = PremiumGold,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "RECOMMENDED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(
                                horizontal = 8.dp,
                                vertical = 4.dp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.spacingSmall))

            // Price
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "$${plan.price.toInt()}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (plan.isRecommended) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Text(
                    text = "/${plan.billingPeriod}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (plan.isRecommended) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    },
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.spacingMedium))

            HorizontalDivider(
                color = if (plan.isRecommended) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                }
            )

            Spacer(modifier = Modifier.height(Dimensions.spacingMedium))

            // Features
            plan.features.forEach { feature ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = if (plan.isRecommended) {
                            PremiumGold
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(Dimensions.spacingSmall))
                    Text(
                        text = feature,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (plan.isRecommended) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.spacingLarge))

            // Subscribe Button
            SWPrimaryButton(
                text = "Subscribe Now",
                onClick = onSubscribe,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
