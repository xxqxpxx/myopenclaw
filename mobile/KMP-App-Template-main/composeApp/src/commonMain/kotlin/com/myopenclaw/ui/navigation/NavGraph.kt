package com.myopenclaw.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.myopenclaw.data.local.PreferencesManager
import com.myopenclaw.ui.screens.auth.*
import com.myopenclaw.ui.screens.chat.ChatScreen
import com.myopenclaw.ui.screens.chat.ConversationListScreen
import com.myopenclaw.ui.screens.home.HomeScreen
import com.myopenclaw.ui.screens.profile.ProfileScreen
import com.myopenclaw.ui.screens.subscription.SubscriptionPlansScreenRevenueCat
import com.myopenclaw.ui.screens.subscription.SubscriptionRequiredScreen
import com.myopenclaw.ui.screens.splash.SplashScreen
import com.myopenclaw.ui.screens.settings.SettingsScreen
import com.myopenclaw.ui.viewmodel.auth.AuthState
import com.myopenclaw.ui.viewmodel.auth.OnboardingViewModel
import com.myopenclaw.ui.viewmodel.auth.SignInViewModel
import com.myopenclaw.ui.viewmodel.auth.SignUpViewModel
import com.myopenclaw.ui.viewmodel.subscription.RevenueCatViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object SignUp : Screen("signup")
    data object SignIn : Screen("signin")
    data object ForgotPassword : Screen("forgot_password")

    // Onboarding Flow
    data object EmailSignUp : Screen("email_signup")
    data object PasswordSetup : Screen("password_setup")
    data object FeatureShowcase : Screen("feature_showcase")
    data object Testimonials : Screen("testimonials")
    data object Questionnaire : Screen("questionnaire")
    data object Setup : Screen("setup")
    data object Paywall : Screen("paywall")
    data object FinalCTA : Screen("final_cta")

    // Main App
    data object Home : Screen("home")
    data object Conversations : Screen("conversations")
    data object Chat : Screen("chat/{conversationId}") {
        fun createRoute(conversationId: String?) =
            if (conversationId != null) "chat/$conversationId" else "chat/new"
    }
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")

    // Subscription
    data object Subscription : Screen("subscription")
    data object SubscriptionRequired : Screen("subscription_required")
}

@Composable
fun AppNavigation(
    preferencesManager: PreferencesManager
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val screensWithBottomNav = listOf(
        Screen.Home.route,
        Screen.Conversations.route,
        Screen.Profile.route
    )

    val showBottomBar = currentRoute in screensWithBottomNav

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    selectedRoute = currentRoute ?: Screen.Home.route,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // ── Auth Flow ──────────────────────────────────────

            composable(Screen.Splash.route) {
                SplashScreen(
                    preferencesManager = preferencesManager,
                    onNavigateToOnboarding = {
                        navController.navigate(Screen.SignUp.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToPaywall = {
                        navController.navigate(Screen.SubscriptionRequired.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onNavigateToEmailAuth = { navController.navigate(Screen.SignUp.route) },
                    onNavigateToGoogleAuth = { navController.navigate(Screen.SignUp.route) }
                )
            }

            composable(Screen.SignUp.route) {
                val signUpViewModel: SignUpViewModel = koinViewModel()
                val authState by signUpViewModel.state.collectAsState()
                val sessionManager: com.myopenclaw.data.session.SessionManager = koinInject()

                LaunchedEffect(authState) {
                    when (authState) {
                        is AuthState.Success -> {
                            signUpViewModel.resetState()
                            val hasCompletedOnboarding = sessionManager.hasCompletedOnboarding()
                            if (hasCompletedOnboarding) {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                navController.navigate(Screen.FeatureShowcase.route) {
                                    popUpTo(Screen.SignUp.route) { inclusive = true }
                                }
                            }
                        }
                        else -> {}
                    }
                }

                if (authState is AuthState.Error) {
                    AlertDialog(
                        onDismissRequest = { signUpViewModel.resetState() },
                        title = { Text("Sign Up Failed") },
                        text = { Text((authState as AuthState.Error).message) },
                        confirmButton = {
                            TextButton(onClick = { signUpViewModel.resetState() }) { Text("OK") }
                        }
                    )
                }

                SignUpScreen(
                    viewModel = signUpViewModel,
                    onNavigateToSignIn = { navController.navigate(Screen.SignIn.route) },
                    onNavigateToEmailSignUp = { navController.navigate(Screen.EmailSignUp.route) }
                )
            }

            composable(Screen.SignIn.route) {
                val signInViewModel: SignInViewModel = koinViewModel()
                val authState by signInViewModel.state.collectAsState()

                LaunchedEffect(authState) {
                    if (authState is AuthState.Success) {
                        signInViewModel.resetState()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }

                if (authState is AuthState.Error) {
                    AlertDialog(
                        onDismissRequest = { signInViewModel.resetState() },
                        title = { Text("Sign In Failed") },
                        text = { Text((authState as AuthState.Error).message) },
                        confirmButton = {
                            TextButton(onClick = { signInViewModel.resetState() }) { Text("OK") }
                        }
                    )
                }

                SignInScreen(
                    viewModel = signInViewModel,
                    onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                    onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
                )
            }

            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.EmailSignUp.route) {
                EmailSignUpScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPasswordSetup = { email ->
                        navController.navigate(Screen.PasswordSetup.route)
                    }
                )
            }

            composable(Screen.PasswordSetup.route) {
                PasswordSetupScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onPasswordSet = {
                        navController.navigate(Screen.FeatureShowcase.route) {
                            popUpTo(Screen.SignUp.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.FeatureShowcase.route) {
                OnboardingFeatureShowcaseScreen(
                    onContinue = { navController.navigate(Screen.Testimonials.route) },
                    onSkip = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Testimonials.route) {
                OnboardingTestimonialsScreen(
                    onContinue = { navController.navigate(Screen.Questionnaire.route) },
                    onSkip = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Questionnaire.route) {
                val onboardingViewModel: OnboardingViewModel = koinInject()
                OnboardingQuestionnaireScreen(
                    viewModel = onboardingViewModel,
                    onContinue = { navController.navigate(Screen.Setup.route) },
                    onSkip = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Setup.route) {
                val onboardingViewModel: OnboardingViewModel = koinInject()
                OnboardingSetupScreen(
                    viewModel = onboardingViewModel,
                    onContinue = {
                        navController.navigate(Screen.FinalCTA.route)
                    }
                )
            }

            composable(Screen.Paywall.route) {
                OnboardingPaywallScreen(
                    onSubscribe = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onSkip = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.FinalCTA.route) {
                val onboardingViewModel: OnboardingViewModel = koinInject()
                val sessionManager: com.myopenclaw.data.session.SessionManager = koinInject()
                OnboardingFinalCTAScreen(
                    viewModel = onboardingViewModel,
                    onGetStarted = {
                        onboardingViewModel.completeOnboarding(sessionManager)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // ── Main App ──────────────────────────────────────

            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToChat = {
                        navController.navigate(Screen.Chat.createRoute(null))
                    },
                    onNavigateToConversations = {
                        navController.navigate(Screen.Conversations.route)
                    }
                )
            }

            composable(Screen.Conversations.route) {
                ConversationListScreen(
                    onOpenChat = { conversationId ->
                        navController.navigate(Screen.Chat.createRoute(conversationId))
                    }
                )
            }

            composable(Screen.Chat.route) { backStackEntry ->
                val conversationId = backStackEntry.arguments?.getString("conversationId")
                    ?.takeIf { it != "new" }

                ChatScreen(
                    conversationId = conversationId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToSubscription = { navController.navigate(Screen.Subscription.route) },
                    onSignOut = {
                        navController.navigate(Screen.SignUp.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // ── Subscription ──────────────────────────────────

            composable(Screen.Subscription.route) {
                SubscriptionPlansScreenRevenueCat(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.SubscriptionRequired.route) {
                SubscriptionRequiredScreen(
                    onSubscribe = {
                        navController.navigate(Screen.Subscription.route)
                    },
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
