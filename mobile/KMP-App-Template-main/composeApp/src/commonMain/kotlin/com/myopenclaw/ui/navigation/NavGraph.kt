package com.myopenclaw.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.myopenclaw.data.local.PreferencesManager
import com.myopenclaw.ui.screens.auth.*
import com.myopenclaw.ui.screens.home.HomeScreen
import com.myopenclaw.ui.screens.home.HomeScreenNew
import com.myopenclaw.ui.screens.markets.MarketsScreen
import com.myopenclaw.ui.screens.insights.InsightsScreen
import com.myopenclaw.ui.screens.watchlist.WatchlistScreenNew
import com.myopenclaw.ui.screens.watchlist.AddStockScreen
import com.myopenclaw.ui.viewmodel.watchlist.WatchlistViewModel
import com.myopenclaw.ui.screens.profile.ProfileScreen
import com.myopenclaw.ui.screens.subscription.SubscriptionPlansScreen
import com.myopenclaw.ui.screens.subscription.SubscriptionPlansScreenRevenueCat
import com.myopenclaw.ui.screens.subscription.SubscriptionRequiredScreen
import com.myopenclaw.domain.repository.RevenueCatRepository
import com.myopenclaw.ui.screens.splash.SplashScreen
import com.myopenclaw.ui.screens.marketdata.*
import com.myopenclaw.ui.screens.insider.InsiderTradingListScreen
import com.myopenclaw.ui.screens.congress.CongressionalTradingListScreen
import com.myopenclaw.ui.screens.options.OptionsFlowListScreen
import com.myopenclaw.ui.screens.ai.AIChatScreen
import com.myopenclaw.ui.viewmodel.auth.AuthState
import com.myopenclaw.ui.viewmodel.auth.OnboardingAnswers
import com.myopenclaw.ui.viewmodel.auth.OnboardingViewModel
import com.myopenclaw.ui.viewmodel.auth.SignInViewModel
import com.myopenclaw.ui.viewmodel.auth.SignUpViewModel
import com.myopenclaw.ui.viewmodel.subscription.SubscriptionViewModel
import com.myopenclaw.ui.viewmodel.subscription.RevenueCatViewModel
import com.myopenclaw.ui.viewmodel.signals.SignalsViewModel
import com.myopenclaw.ui.viewmodel.signals.SignalsState
import com.myopenclaw.domain.usecase.signals.MarketSignalAccess
import com.myopenclaw.ui.viewmodel.analysis.AnalysisHistoryViewModel
import com.myopenclaw.ui.viewmodel.analysis.AnalysisState
import com.myopenclaw.ui.viewmodel.ai.ChartAnalysisViewModel
import com.myopenclaw.util.UrlLauncher
import com.myopenclaw.ui.components.LanguageSelectionDialog
import com.myopenclaw.ui.viewmodel.profile.ProfileState
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject

// Extension function to get string argument from BackStackEntry for KMP
@Suppress("DEPRECATION")
private fun androidx.navigation.NavBackStackEntry.getArg(key: String): String? {
    return try {
        // Access the arguments using reflection-free approach for KMP compatibility
        val args = this.arguments ?: return null
        // The savedStateHandle should contain the arguments
        this.savedStateHandle.get<String>(key)
    } catch (e: Exception) {
        null
    }
}

sealed class Screen(val route: String) {
    // Auth Routes
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object SignUp : Screen("signup")
    data object SignIn : Screen("signin")
    data object ForgotPassword : Screen("forgot_password")

    // New Onboarding Flow Routes
    data object EmailSignUp : Screen("email_signup")
    data object PasswordSetup : Screen("password_setup")
    data object FeatureShowcase : Screen("feature_showcase")
    data object Testimonials : Screen("testimonials")
    data object Questionnaire : Screen("questionnaire")
    data object Setup : Screen("setup")
    data object Paywall : Screen("paywall")
    data object FinalCTA : Screen("final_cta")

    // Main App Routes
    data object Home : Screen("home")
    data object Markets : Screen("markets")
    data object Insights : Screen("insights")
    data object Watchlist : Screen("watchlist")
    data object AddStock : Screen("add_stock")
    data object Profile : Screen("profile")

    // Subscription
    data object Subscription : Screen("subscription")
    data object SubscriptionRequired : Screen("subscription_required")
    data object AlertSettings : Screen("alert_settings")

    // Market Data Screens
    data object SocialSentiment : Screen("social_sentiment")
    data object DarkPool : Screen("dark_pool")
    data object GovernmentContracts : Screen("government_contracts")
    data object LobbyistActivity : Screen("lobbyist_activity")
    data object PoliticalDonations : Screen("political_donations")
    data object InsiderTrading : Screen("insider_trading")
    data object CongressTrading : Screen("congress_trading")
    data object OptionsFlow : Screen("options_flow")
    data object AIChat : Screen("ai_chat")
    data object AITradeIdeas : Screen("ai_trade_ideas")
    data object AnalysisHistory : Screen("analysis_history")
    data object ChartAnalysis : Screen("chart_analysis")
    data object ChartAnalysisResults : Screen("chart_analysis_results/{analysisId}") {
        fun createRoute(analysisId: String) = "chart_analysis_results/$analysisId"
    }
    data object MarketSignals : Screen("market_signals")
    data object HowSignalsWork : Screen("how_signals_work")
    data object SignalDetails : Screen("signal_details/{signalId}") {
        fun createRoute(signalId: String) = "signal_details/$signalId"
    }
    data object Search : Screen("search")
    data object StockDetails : Screen("stock_details/{ticker}") {
        fun createRoute(ticker: String) = "stock_details/$ticker"
    }
}

@Composable
fun AppNavigation(
    preferencesManager: PreferencesManager
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Screens that should show bottom navigation
    val screensWithBottomNav = listOf(
        Screen.Home.route,
        Screen.Markets.route,
        Screen.Insights.route,
        Screen.Watchlist.route,
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
                            // Pop up to the start destination to avoid building up a large stack
                            popUpTo(Screen.Home.route) { saveState = true }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
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
            // Auth Flow
            composable(Screen.Splash.route) {
                SplashScreen(
                    preferencesManager = preferencesManager,
                    onNavigateToOnboarding = {
                        // Navigate directly to SignUp (landing page), not old onboarding
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
                        // Navigate to subscription required screen for non-subscribed users
                        navController.navigate(Screen.SubscriptionRequired.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // Old OnboardingScreen - Deprecated, keeping for backwards compatibility
            // The new flow goes: Splash → SignUp → EmailSignUp → FeatureShowcase → ...
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onNavigateToEmailAuth = {
                        navController.navigate(Screen.SignUp.route)
                    },
                    onNavigateToGoogleAuth = {
                        navController.navigate(Screen.SignUp.route)
                    }
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
                                // Returning user - go to Home immediately, subscription check happens there
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                // New user - start onboarding flow
                                navController.navigate(Screen.FeatureShowcase.route) {
                                    popUpTo(Screen.SignUp.route) { inclusive = true }
                                }
                            }
                        }
                        else -> {}
                    }
                }

                // Show error dialog
                if (authState is AuthState.Error) {
                    AlertDialog(
                        onDismissRequest = { signUpViewModel.resetState() },
                        title = { Text("Sign Up Failed") },
                        text = { Text((authState as AuthState.Error).message) },
                        confirmButton = {
                            TextButton(onClick = { signUpViewModel.resetState() }) {
                                Text("OK")
                            }
                        }
                    )
                }

                SignUpScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToSignIn = {
                        navController.navigate(Screen.SignIn.route) {
                            popUpTo(Screen.SignUp.route) { inclusive = true }
                        }
                    },
                    onNavigateToEmailSignUp = {
                        navController.navigate(Screen.EmailSignUp.route)
                    },
                    onNavigateToGoogleSignUp = {
                        // Trigger Google Sign-In
                        signUpViewModel.signUpWithGoogle()
                    },
                    onNavigateToAppleSignUp = {
                        // Trigger Apple Sign-In
                        signUpViewModel.signUpWithApple()
                    }
                )
            }

            // New Onboarding Flow
            composable(Screen.EmailSignUp.route) {
                val onboardingViewModel: OnboardingViewModel = koinInject()

                EmailSignUpScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onContinue = { email, name ->
                        // println("NavGraph (EmailSignUp): onContinue called with email='$email', name='$name'")
                        // Save email and name to OnboardingViewModel (we'll need them for account creation)
                        onboardingViewModel.saveUserEmail(email)
                        onboardingViewModel.saveUserName(name)
                        // println("NavGraph (EmailSignUp): Saved to ViewModel, navigating to PasswordSetup")
                        // Navigate to password setup screen
                        navController.navigate(Screen.PasswordSetup.route)
                    }
                )
            }

            composable(Screen.PasswordSetup.route) {
                val onboardingViewModel: OnboardingViewModel = koinInject()
                val onboardingData by onboardingViewModel.onboardingData.collectAsState()

                // println("NavGraph (PasswordSetup): Composable entered, onboardingData.userEmail='${onboardingData.userEmail}', onboardingData.userName='${onboardingData.userName}'")

                PasswordSetupScreen(
                    email = onboardingData.userEmail,
                    displayName = onboardingData.userName,
                    onNavigateBack = { navController.popBackStack() },
                    onAccountCreated = {
                        // println("NavGraph (PasswordSetup): Account created successfully, navigating to Questionnaire")
                        // Account created successfully, start with questionnaire
                        // Clear the entire auth flow from back stack
                        navController.navigate(Screen.Questionnaire.route) {
                            popUpTo(Screen.SignUp.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Testimonials.route) {
                OnboardingTestimonialsScreen(
                    onContinue = {
                        navController.navigate(Screen.Setup.route)
                    }
                )
            }

            composable(Screen.FeatureShowcase.route) {
                OnboardingFeatureShowcaseScreen(
                    onContinue = {
                        navController.navigate(Screen.Testimonials.route)
                    }
                )
            }

            composable(Screen.Questionnaire.route) {
                val onboardingViewModel: OnboardingViewModel = koinInject()
                val onboardingData by onboardingViewModel.onboardingData.collectAsState()

                OnboardingQuestionnaireScreen(
                    userName = onboardingData.userName.ifEmpty { "there" },
                    onComplete = { answers ->
                        // Save questionnaire answers to PreferencesManager
                        onboardingViewModel.saveQuestionnaireAnswers(answers)
                        navController.navigate(Screen.FeatureShowcase.route)
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Setup.route) {
                val onboardingViewModel: OnboardingViewModel = koinInject()
                val onboardingData by onboardingViewModel.onboardingData.collectAsState()

                OnboardingSetupScreen(
                    userName = onboardingData.userName.ifEmpty { "there" },
                    onSetupComplete = {
                        navController.navigate(Screen.Paywall.route)
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Paywall.route) {
                val onboardingViewModel: OnboardingViewModel = koinInject()
                val subscriptionViewModel: SubscriptionViewModel = koinViewModel()
                val revenueCatViewModel: RevenueCatViewModel = koinViewModel()
                val urlLauncher: UrlLauncher = koinInject()
                val onboardingData by onboardingViewModel.onboardingData.collectAsState()

                OnboardingPaywallScreen(
                    userName = onboardingData.userName.ifEmpty { "there" },
                    variant = PaywallVariant.DISCOUNT_EMPHASIS, // HARD PAYWALL - requires real payment
                    subscriptionViewModel = subscriptionViewModel,
                    revenueCatViewModel = revenueCatViewModel,
                    onContinue = { plan, wantsFreeTrial ->
                        // This callback is not used in hard paywall mode
                        // Payment must complete through onPurchaseSuccess
                        // println("NavGraph (Paywall): onContinue called - this should not happen in hard paywall mode")
                    },
                    onSkip = {
                        // Allow user to preview app without payment
                        // println("NavGraph (Paywall): User selected 'Try without payment' - allowing preview")
                        // Mark onboarding as complete but don't mark subscription as purchased
                        onboardingViewModel.completeOnboarding()
                        // Navigate to Home screen for preview
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true } // Clear entire back stack
                        }
                    },
                    onCheckoutUrlReady = { url ->
                        // println("NavGraph (Paywall): Opening Stripe checkout URL: $url")
                        urlLauncher.openUrl(url)
                        // User must complete payment - don't auto-navigate
                    },
                    onPurchaseSuccess = {
                        // println("NavGraph (Paywall): RevenueCat purchase successful! Navigating to FinalCTA")
                        // Save subscription as purchased and continue to final screen
                        onboardingViewModel.saveSubscriptionChoice("purchased", false)
                        navController.navigate(Screen.FinalCTA.route)
                    }
                )
            }

            composable(Screen.FinalCTA.route) {
                val onboardingViewModel: OnboardingViewModel = koinInject()

                OnboardingFinalCTAScreen(
                    onTakePhoto = {
                        // println("NavGraph (FinalCTA): onTakePhoto - Completing onboarding and navigating to Home")
                        // Mark onboarding as complete
                        onboardingViewModel.completeOnboarding()
                        // Navigate to Home (camera feature to be implemented)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true } // Clear entire back stack
                        }
                    },
                    onGetMarketTrends = {
                        // println("NavGraph (FinalCTA): onGetMarketTrends - Completing onboarding and navigating to Home")
                        // Mark onboarding as complete
                        onboardingViewModel.completeOnboarding()
                        // Navigate to Home/Markets
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true } // Clear entire back stack
                        }
                    }
                )
            }

            composable(Screen.SignIn.route) {
                val signInViewModel: SignInViewModel = koinViewModel()
                val authState by signInViewModel.state.collectAsState()
                val sessionManager: com.myopenclaw.data.session.SessionManager = koinInject()

                LaunchedEffect(authState) {
                    when (authState) {
                        is AuthState.Success -> {
                            // Navigate to Home immediately - subscription check happens there
                            signInViewModel.resetState()
                            navController.navigate(Screen.Home.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                        else -> {}
                    }
                }

                SignInScreen(
                    onNavigateBack = {
                        // Try to pop back stack, if nothing to pop, navigate to SignUp
                        if (!navController.popBackStack()) {
                            navController.navigate(Screen.SignUp.route) {
                                popUpTo(Screen.SignIn.route) { inclusive = true }
                            }
                        }
                    },
                    onNavigateToSignUp = {
                        // println("NavGraph (SignIn): Navigating to SignUp")
                        navController.navigate(Screen.SignUp.route) {
                            popUpTo(Screen.SignIn.route) { inclusive = true }
                        }
                    },
                    onNavigateToForgotPassword = {
                        // println("NavGraph (SignIn): Navigating to ForgotPassword")
                        navController.navigate(Screen.ForgotPassword.route)
                    },
                    onNavigateToHome = {
                        // This is now handled by LaunchedEffect above
                        // println("NavGraph (SignIn): onNavigateToHome called (handled by LaunchedEffect)")
                    },
                    viewModel = signInViewModel
                )
            }

            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Main App Screens
            composable(Screen.Home.route) {
                // Inject ViewModels
                val signalsViewModel: SignalsViewModel = koinViewModel()
                val analysisViewModel: AnalysisHistoryViewModel = koinViewModel()
                val revenueCatRepository: RevenueCatRepository = koinInject()
                val sessionManager: com.myopenclaw.data.session.SessionManager = koinInject()

                // Background subscription check - redirects non-subscribed users to paywall
                LaunchedEffect(Unit) {
                    val bypassEmail = "ahmedafatah@outlook.com"
                    val userEmail = sessionManager.getCurrentUserEmail()?.lowercase()
                    if (userEmail != bypassEmail) {
                        val hasActive = try {
                            revenueCatRepository.hasActiveSubscription()
                        } catch (_: Exception) {
                            false
                        }
                        if (!hasActive) {
                            navController.navigate(Screen.SubscriptionRequired.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    }
                }

                // Collect state from ViewModels
                val signalsState by signalsViewModel.signalsState.collectAsState()
                val analysisState by analysisViewModel.analysisState.collectAsState()
                val signalAccess by signalsViewModel.signalAccessState.collectAsState()

                // Note: access status is checked in SignalsViewModel.init() and cached
                // in RevenueCatRepositoryImpl. No need for redundant refresh here
                // as it caused duplicate SDK calls that froze iPad.

                // Extract data from states
                val signals = when (signalsState) {
                    is SignalsState.Success -> (signalsState as SignalsState.Success).signals
                    else -> emptyList()
                }

                val analyses = when (analysisState) {
                    is AnalysisState.Success -> (analysisState as AnalysisState.Success).analyses
                    else -> emptyList()
                }

                val isLoadingSignals = signalsState is SignalsState.Loading
                val isLoadingAnalyses = analysisState is AnalysisState.Loading

                HomeScreenNew(
                    signals = signals,
                    analyses = analyses,
                    isLoadingSignals = isLoadingSignals,
                    isLoadingAnalyses = isLoadingAnalyses,
                    signalAccess = signalAccess,
                    onAnalyseTradeClick = {
                        // println("NavGraph: Navigating to Chart Analysis")
                        navController.navigate(Screen.ChartAnalysis.route)
                    },
                    onAskQuestionsClick = {
                        // println("NavGraph: Navigating to AI Chat")
                        navController.navigate(Screen.AIChat.route)
                    },
                    onSignalClick = { signal ->
                        // println("NavGraph: Navigating to Signal Details: ${signal.id}")
                        navController.navigate(Screen.SignalDetails.createRoute(signal.id))
                    },
                    onAnalysisClick = { analysisId ->
                        // println("NavGraph: Navigating to Analysis Details: $analysisId")
                        navController.navigate(Screen.ChartAnalysisResults.createRoute(analysisId))
                    },
                    onViewAllSignalsClick = {
                        // println("NavGraph: Navigating to Market Signals")
                        navController.navigate(Screen.MarketSignals.route)
                    },
                    onViewAllAnalysisClick = {
                        // println("NavGraph: Navigating to Analysis History")
                        navController.navigate(Screen.AnalysisHistory.route)
                    },
                    onScanClick = {
                        // println("NavGraph: Navigating to Chart Analysis")
                        navController.navigate(Screen.ChartAnalysis.route)
                    },
                    onSubscribeClick = {
                        // println("NavGraph: Navigating to Subscription from upsell banner")
                        navController.navigate(Screen.Subscription.route)
                    }
                )
            }

            composable(Screen.Markets.route) {
                MarketsScreen(
                    onNavigateToInsiderTrading = { navController.navigate(Screen.InsiderTrading.route) },
                    onNavigateToCongressTrading = { navController.navigate(Screen.CongressTrading.route) },
                    onNavigateToOptionsFlow = { navController.navigate(Screen.OptionsFlow.route) },
                    onNavigateToAIChat = { navController.navigate(Screen.AIChat.route) },
                    onNavigateToSocialSentiment = { navController.navigate(Screen.SocialSentiment.route) },
                    onNavigateToDarkPool = { navController.navigate(Screen.DarkPool.route) },
                    onNavigateToGovernmentContracts = { navController.navigate(Screen.GovernmentContracts.route) },
                    onNavigateToLobbyistActivity = { navController.navigate(Screen.LobbyistActivity.route) },
                    onNavigateToPoliticalDonations = { navController.navigate(Screen.PoliticalDonations.route) },
                    onNavigateToStockDetails = { ticker -> navController.navigate(Screen.StockDetails.createRoute(ticker)) },
                    onNavigateToAlerts = { navController.navigate(Screen.AlertSettings.route) },
                    onNavigateToSearch = { navController.navigate(Screen.Search.route) }
                )
            }

            composable(Screen.Insights.route) {
                InsightsScreen(
                    onNavigateToAIChat = { navController.navigate(Screen.AIChat.route) },
                    onNavigateToTradeIdeas = { navController.navigate(Screen.AITradeIdeas.route) },
                    onNavigateToChartAnalysis = { navController.navigate(Screen.ChartAnalysis.route) },
                    onNavigateToAnalysisHistory = { navController.navigate(Screen.AnalysisHistory.route) },
                    onNavigateToAnalysisDetails = { analysisId ->
                        navController.navigate(Screen.ChartAnalysisResults.createRoute(analysisId))
                    }
                )
            }

            composable(Screen.Watchlist.route) {
                val watchlistViewModel: WatchlistViewModel = koinViewModel()

                // Observe result from AddStock screen
                val savedStateHandle = it.savedStateHandle
                val stockAdded = savedStateHandle.get<Boolean>("stock_added") == true
                LaunchedEffect(stockAdded) {
                    if (stockAdded) {
                        watchlistViewModel.loadWatchlist()
                        savedStateHandle.remove<Boolean>("stock_added")
                    }
                }

                WatchlistScreenNew(
                    viewModel = watchlistViewModel,
                    onNavigateToAddStock = {
                        navController.navigate(Screen.AddStock.route)
                    },
                    onNavigateToStockDetails = { ticker ->
                        navController.navigate(Screen.StockDetails.createRoute(ticker))
                    }
                )
            }

            composable(Screen.AddStock.route) {
                AddStockScreen(
                    onNavigateBack = {
                        navController.previousBackStackEntry?.savedStateHandle?.set("stock_added", true)
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Profile.route) {
                val profileViewModel: com.myopenclaw.ui.viewmodel.profile.ProfileViewModel = koinViewModel()
                val platformUtils = com.myopenclaw.util.getPlatformUtils()
                val profileState by profileViewModel.profileState.collectAsState()

                // State for language selection dialog
                var showLanguageDialog by remember { mutableStateOf(false) }

                // Get current language from profile
                val currentLanguage = if (profileState is ProfileState.Success) {
                    (profileState as ProfileState.Success).profile.preferences.language
                } else {
                    "English"
                }

                ProfileScreen(
                    viewModel = profileViewModel,
                    onSubscriptionClick = {
                        navController.navigate(Screen.Subscription.route)
                    },
                    onRateAppClick = {
                        platformUtils.rateApp()
                    },
                    onShareAppClick = {
                        platformUtils.shareText(
                            text = "Check out Signalwhisper - The best trading signals app! Download now: https://myopenclaw.com/download",
                            subject = "Try Signalwhisper App"
                        )
                    },
                    onChangeLanguageClick = {
                        showLanguageDialog = true
                    },
                    onChatClick = {
                        platformUtils.openEmail(
                            email = "support@myopenclaw.com",
                            subject = "Support Request - Signalwhisper App"
                        )
                    },
                    onTermsClick = {
                        platformUtils.openUrl("https://myopenclaw.com/terms")
                    },
                    onPrivacyClick = {
                        platformUtils.openUrl("https://myopenclaw.com/privacy")
                    },
                    onSignOutSuccess = {
                        navController.navigate(Screen.SignIn.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onDeleteAccountSuccess = {
                        navController.navigate(Screen.SignIn.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )

                // Show language selection dialog
                if (showLanguageDialog) {
                    LanguageSelectionDialog(
                        currentLanguage = currentLanguage,
                        onLanguageSelected = { language ->
                            profileViewModel.updateLanguage(language)
                        },
                        onDismiss = {
                            showLanguageDialog = false
                        }
                    )
                }
            }

            composable(Screen.Subscription.route) {
                val profileViewModel: com.myopenclaw.ui.viewmodel.profile.ProfileViewModel = koinViewModel()

                SubscriptionPlansScreenRevenueCat(
                    onNavigateBack = { navController.popBackStack() },
                    onPurchaseSuccess = {
                        // println("NavGraph (Subscription): Purchase successful! Refreshing profile and navigating back")
                        // Refresh subscription status after purchase
                        profileViewModel.refreshSubscriptionStatus()
                        profileViewModel.loadUserProfile()
                        // Navigate back to profile
                        navController.popBackStack()
                    }
                )
            }

                        // Alert Settings Screen
            composable(Screen.AlertSettings.route) {
                com.myopenclaw.ui.screens.alerts.AlertSettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Subscription Required Screen - Hard paywall for non-subscribed users
            composable(Screen.SubscriptionRequired.route) {
                val revenueCatViewModel: RevenueCatViewModel = koinViewModel()
                val sessionManager: com.myopenclaw.data.session.SessionManager = koinInject()

                SubscriptionRequiredScreen(
                    revenueCatViewModel = revenueCatViewModel,
                    onSubscriptionSuccess = {
                        // println("NavGraph (SubscriptionRequired): Subscription successful, navigating to Home")
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onSkipToHome = {
                        // println("NavGraph (SubscriptionRequired): User skipped subscription, navigating to Home")
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onSignOut = {
                        // println("NavGraph (SubscriptionRequired): User signing out")
                        sessionManager.clearSession()
                        navController.navigate(Screen.SignIn.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // Market Data Screens
            composable(Screen.SocialSentiment.route) {
                SocialSentimentScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetails = { ticker ->
                        navController.navigate(Screen.StockDetails.createRoute(ticker))
                    }
                )
            }

            composable(Screen.DarkPool.route) {
                DarkPoolScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetails = { ticker ->
                        navController.navigate(Screen.StockDetails.createRoute(ticker))
                    }
                )
            }

            composable(Screen.GovernmentContracts.route) {
                GovernmentContractsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetails = { ticker ->
                        navController.navigate(Screen.StockDetails.createRoute(ticker))
                    }
                )
            }

            composable(Screen.LobbyistActivity.route) {
                LobbyistActivityScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetails = { ticker ->
                        navController.navigate(Screen.StockDetails.createRoute(ticker))
                    }
                )
            }

            composable(Screen.PoliticalDonations.route) {
                PoliticalDonationsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetails = { ticker ->
                        navController.navigate(Screen.StockDetails.createRoute(ticker))
                    }
                )
            }

            composable(Screen.InsiderTrading.route) {
                InsiderTradingListScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetails = { ticker ->
                        navController.navigate(Screen.StockDetails.createRoute(ticker))
                    }
                )
            }

            composable(Screen.CongressTrading.route) {
                CongressionalTradingListScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetails = { ticker ->
                        navController.navigate(Screen.StockDetails.createRoute(ticker))
                    }
                )
            }

            composable(Screen.OptionsFlow.route) {
                OptionsFlowListScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetails = { ticker ->
                        navController.navigate(Screen.StockDetails.createRoute(ticker))
                    }
                )
            }

            composable(Screen.AIChat.route) {
                AIChatScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToSubscription = {
                        navController.navigate(Screen.Subscription.route)
                    }
                )
            }

            // AI Trade Ideas
            composable(Screen.AITradeIdeas.route) {
                com.myopenclaw.ui.screens.ai.AITradeIdeasScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetails = { ticker ->
                        navController.navigate(Screen.StockDetails.createRoute(ticker))
                    },
                    onNavigateToChat = { navController.navigate(Screen.AIChat.route) }
                )
            }

            // Analysis History
            composable(Screen.AnalysisHistory.route) {
                com.myopenclaw.ui.screens.ai.AnalysisHistoryScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onAnalysisClick = { analysis ->
                        navController.navigate(Screen.ChartAnalysisResults.createRoute(analysis.id))
                    }
                )
            }

            // Chart Analysis - use shared ViewModel for upload and results screens
            composable(Screen.ChartAnalysis.route) { backStackEntry ->
                val imagePicker: com.myopenclaw.util.ImagePicker = koinInject()
                // Create the ViewModel scoped to this backstack entry
                val chartAnalysisViewModel: ChartAnalysisViewModel = koinViewModel()

                com.myopenclaw.ui.screens.ai.ChartAnalysisUploadScreen(
                    viewModel = chartAnalysisViewModel,
                    imagePicker = imagePicker,
                    onNavigateBack = { navController.popBackStack() },
                    onViewResults = {
                        // Navigate to results screen - the ViewModel state will be accessed via parent entry
                        navController.navigate(Screen.ChartAnalysisResults.createRoute("latest"))
                    }
                )
            }

            // Chart Analysis Results
            composable(
                route = Screen.ChartAnalysisResults.route,
                arguments = listOf(navArgument("analysisId") { type = NavType.StringType })
            ) { backStackEntry ->
                val analysisId = backStackEntry.getArg("analysisId") ?: "latest"

                if (analysisId == "latest") {
                    // Get the ViewModel from the ChartAnalysis parent backstack entry to share state
                    val parentEntry = remember(backStackEntry) {
                        try {
                            navController.getBackStackEntry(Screen.ChartAnalysis.route)
                        } catch (e: Exception) {
                            null
                        }
                    }

                    val chartAnalysisViewModel: ChartAnalysisViewModel = if (parentEntry != null) {
                        koinViewModel(viewModelStoreOwner = parentEntry)
                    } else {
                        koinViewModel()
                    }
                    val platformUtils = com.myopenclaw.util.getPlatformUtils()

                    com.myopenclaw.ui.screens.ai.ChartAnalysisResultsScreenNew(
                        viewModel = chartAnalysisViewModel,
                        onNavigateBack = {
                            if (!navController.popBackStack()) {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        },
                        onNavigateToUpgrade = { navController.navigate(Screen.Subscription.route) },
                        onSaveAnalysis = {
                            chartAnalysisViewModel.saveCurrentAnalysis()
                        },
                        onSetAlert = {
                            // Handled by bottom sheet inside the screen
                        },
                        onShareAnalysis = { shareText ->
                            platformUtils.shareText(
                                text = shareText,
                                subject = "Chart Analysis - Signal Whisper"
                            )
                        }
                    )
                } else {
                    // Show saved analysis details
                    com.myopenclaw.ui.screens.ai.ChartAnalysisDetailsScreen(
                        analysisId = analysisId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }

            // Market Signals List
            composable(Screen.MarketSignals.route) {
                com.myopenclaw.ui.screens.markets.MarketSignalsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSignalClick = { signalId ->
                        navController.navigate(Screen.SignalDetails.createRoute(signalId))
                    },
                    onHowSignalsWorkClick = {
                        // println("NavGraph: Navigating to How Signals Work")
                        navController.navigate(Screen.HowSignalsWork.route)
                    },
                    onNavigateToSubscription = {
                        navController.navigate(Screen.Subscription.route)
                    }
                )
            }

            // How Signals Work
            composable(Screen.HowSignalsWork.route) {
                com.myopenclaw.ui.screens.markets.HowSignalsWorkScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Signal Details
            composable(
                route = Screen.SignalDetails.route,
                arguments = listOf(navArgument("signalId") { type = NavType.StringType })
            ) { backStackEntry ->
                val signalId = backStackEntry.getArg("signalId") ?: ""
                // println("NavGraph (SignalDetails): Extracted signalId from arguments: $signalId")
                com.myopenclaw.ui.screens.markets.SignalDetailsScreen(
                    signalId = signalId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Search
            composable(Screen.Search.route) {
                com.myopenclaw.ui.screens.search.SearchScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToInsiderDetails = { traderId ->
                        navController.navigate(Screen.InsiderTrading.route)
                    },
                    onNavigateToCongressDetails = { tradeId ->
                        navController.navigate(Screen.CongressTrading.route)
                    },
                    onNavigateToOptionsDetails = { flowId ->
                        navController.navigate(Screen.OptionsFlow.route)
                    }
                )
            }

            // Stock Details (temporarily navigates to Chart Analysis for the ticker)
            composable(
                route = Screen.StockDetails.route,
                arguments = listOf(navArgument("ticker") { type = NavType.StringType })
            ) { backStackEntry ->
                val ticker = backStackEntry.getArg("ticker") ?: ""

                // Stock Details navigation logged

                // For now, redirect to Chart Analysis screen
                // TODO: Create dedicated StockDetailsScreen
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.ChartAnalysis.route) {
                        popUpTo(Screen.StockDetails.route) { inclusive = true }
                    }
                }
            }
        }
    }
}
