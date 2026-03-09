package com.myopenclaw.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.myopenclaw.ui.theme.Dark1
import com.myopenclaw.ui.theme.White
import com.myopenclaw.ui.viewmodel.profile.PricingState
import com.myopenclaw.ui.viewmodel.profile.ProfileViewModel

/**
 * Upgrade Subscription Screen with ViewModel integration
 */
@Composable
fun UpgradeSubscriptionScreenContainer(
    viewModel: ProfileViewModel,
    onContinueClick: (SubscriptionPlan) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val pricingState by viewModel.pricingState.collectAsState()
    val selectedPlan by viewModel.selectedUpgradePlan.collectAsState()

    // Load pricing when screen opens
    LaunchedEffect(Unit) {
        viewModel.loadSubscriptionPricing()
    }

    when (val state = pricingState) {
        is PricingState.Idle -> {
            // Loading will be triggered by LaunchedEffect
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Dark1),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = White)
            }
        }
        is PricingState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Dark1),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = White)
            }
        }
        is PricingState.Success -> {
            // Pricing loaded, show the upgrade screen
            UpgradeSubscriptionScreen(
                selectedPlan = selectedPlan,
                onPlanSelected = { plan ->
                    viewModel.selectUpgradePlan(plan)
                },
                onContinueClick = {
                    selectedPlan?.let { plan ->
                        onContinueClick(plan)
                    }
                }
            )
        }
        is PricingState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Dark1)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Error Loading Pricing",
                        style = MaterialTheme.typography.headlineSmall,
                        color = White
                    )
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = White.copy(alpha = 0.7f)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(onClick = onBackClick) {
                            Text("Go Back")
                        }
                        Button(onClick = { viewModel.loadSubscriptionPricing() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}
