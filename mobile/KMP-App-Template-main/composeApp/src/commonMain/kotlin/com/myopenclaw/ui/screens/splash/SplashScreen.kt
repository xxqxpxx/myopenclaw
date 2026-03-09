package com.myopenclaw.ui.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.myopenclaw.data.auth.AuthManager
import com.myopenclaw.data.local.PreferencesManager
import com.myopenclaw.data.session.SessionManager
import com.myopenclaw.data.session.SessionState
import com.myopenclaw.ui.theme.*
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import myopenclaw.composeapp.generated.resources.Res
import myopenclaw.composeapp.generated.resources.appstore

@Composable
fun SplashScreen(
    preferencesManager: PreferencesManager,
    onNavigateToOnboarding: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToPaywall: () -> Unit = {},
    sessionManager: SessionManager = koinInject()
) {
    // Check authentication and onboarding status after delay
    LaunchedEffect(Unit) {
        // Show splash for minimum 1.5 seconds for branding
        delay(1500)

        // Use SessionManager to check and restore session
        val sessionState = sessionManager.checkSession()
        // println("SplashScreen: Session state = $sessionState")

        // Check if user has completed onboarding
        val hasCompletedOnboarding = sessionManager.hasCompletedOnboarding()
        // println("SplashScreen: hasCompletedOnboarding=$hasCompletedOnboarding")

        // Navigate based on session state
        when (sessionState) {
            is SessionState.Authenticated -> {
                // println("SplashScreen: User authenticated as ${sessionState.user.email}")
                if (hasCompletedOnboarding) {
                    // Navigate to Home immediately - subscription check happens there
                    onNavigateToHome()
                } else {
                    // println("SplashScreen: User needs to complete onboarding")
                    onNavigateToOnboarding()
                }
            }
            is SessionState.Unauthenticated -> {
                // println("SplashScreen: No valid session, navigating to Sign Up")
                onNavigateToOnboarding()
            }
            is SessionState.Loading -> {
                // This shouldn't happen as checkSession waits for completion
                // println("SplashScreen: Still loading (unexpected)")
                onNavigateToOnboarding()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0A0F1E) // Dark navy background from Figma
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // App Icon - Using appstore.png
            Image(
                painter = painterResource(Res.drawable.appstore),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .testTag("splash_app_logo"),
                contentScale = ContentScale.Crop
            )
        }
    }
}
