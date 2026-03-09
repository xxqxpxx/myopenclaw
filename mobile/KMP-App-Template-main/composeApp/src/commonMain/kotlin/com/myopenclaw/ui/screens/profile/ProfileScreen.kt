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
 * Profile Screen with ViewModel integration
 * This composable manages state and delegates to ProfileScreenNew for UI
 */
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onSubscriptionClick: () -> Unit = {},
    onRateAppClick: () -> Unit = {},
    onShareAppClick: () -> Unit = {},
    onChangeLanguageClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onSignOutSuccess: () -> Unit = {},
    onDeleteAccountSuccess: () -> Unit = {}
) {
    val profileState by viewModel.profileState.collectAsState()

    when (val state = profileState) {
        is ProfileState.Loading -> {
            ProfileLoadingScreen()
        }
        is ProfileState.Success -> {
            val profile = state.profile
            ProfileScreenNew(
                isPaidSubscriber = viewModel.isPaidSubscriber(),
                userName = profile.fullName,
                userEmail = profile.email,
                joinedDate = viewModel.getFormattedJoinedDate(),
                billingInfo = viewModel.getBillingInfo(),
                appVersion = "1.16",
                currentLanguage = profile.preferences.language,
                onSubscriptionClick = onSubscriptionClick,
                onRateAppClick = onRateAppClick,
                onShareAppClick = onShareAppClick,
                onChangeLanguageClick = onChangeLanguageClick,
                onChatClick = onChatClick,
                onTermsClick = onTermsClick,
                onPrivacyClick = onPrivacyClick,
                onSignOutClick = {
                    viewModel.signOut(onSuccess = onSignOutSuccess)
                },
                onDeleteAccountClick = {
                    // Show confirmation dialog first
                    // For now, just call the delete
                    viewModel.deleteAccount(
                        reason = null,
                        feedback = null,
                        confirmEmail = profile.email,
                        onSuccess = onDeleteAccountSuccess
                    )
                }
            )
        }
        is ProfileState.Error -> {
            ProfileErrorScreen(
                message = state.message,
                onRetry = { viewModel.loadUserProfile() }
            )
        }
    }
}

/**
 * Loading state UI
 */
@Composable
private fun ProfileLoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Dark1),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = White
        )
    }
}

/**
 * Error state UI
 */
@Composable
private fun ProfileErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
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
                text = "Error Loading Profile",
                style = MaterialTheme.typography.headlineSmall,
                color = White
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = White.copy(alpha = 0.7f)
            )
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}
