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
import com.myopenclaw.ui.viewmodel.profile.ProfileState
import com.myopenclaw.ui.viewmodel.profile.ProfileViewModel

/**
 * Subscription Details Screen with ViewModel integration
 */
@Composable
fun SubscriptionDetailsScreenContainer(
    viewModel: ProfileViewModel,
    onUpgradeClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val profileState by viewModel.profileState.collectAsState()

    when (val state = profileState) {
        is ProfileState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Dark1),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = White)
            }
        }
        is ProfileState.Success -> {
            val profile = state.profile
            val subscription = profile.subscription

            SubscriptionDetailsScreen(
                fullName = profile.fullName,
                emailAddress = profile.email,
                nextBillingDate = subscription.nextBillingDate ?: "N/A",
                subscriptionRenews = subscription.renewalFrequency ?: "N/A",
                billingInfo = viewModel.getBillingInfo(),
                onUpgradeClick = onUpgradeClick,
                onBackClick = onBackClick
            )
        }
        is ProfileState.Error -> {
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
                        text = "Error Loading Subscription",
                        style = MaterialTheme.typography.headlineSmall,
                        color = White
                    )
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = White.copy(alpha = 0.7f)
                    )
                    Button(onClick = { viewModel.loadUserProfile() }) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}
