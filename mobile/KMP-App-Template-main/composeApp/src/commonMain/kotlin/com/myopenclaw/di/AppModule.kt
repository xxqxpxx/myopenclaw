package com.myopenclaw.di

import com.myopenclaw.data.auth.AuthManager
import com.myopenclaw.data.session.SessionManager
import com.myopenclaw.data.remote.ApiService
import com.myopenclaw.data.remote.HttpClientFactory
import com.myopenclaw.data.repository.AuthRepositoryImpl
import com.myopenclaw.data.repository.MarketDataRepositoryImpl
import com.myopenclaw.data.repository.SubscriptionRepositoryImpl
import com.myopenclaw.domain.repository.AuthRepository
import com.myopenclaw.domain.repository.MarketDataRepository
import com.myopenclaw.domain.repository.SubscriptionRepository
import com.myopenclaw.domain.usecase.auth.ResetPasswordUseCase
import com.myopenclaw.domain.usecase.auth.SignInUseCase
import com.myopenclaw.domain.usecase.auth.SignOutUseCase
import com.myopenclaw.domain.usecase.auth.SignUpUseCase
import com.myopenclaw.domain.usecase.subscription.CreateCheckoutSessionUseCase
import com.myopenclaw.domain.usecase.subscription.GetSubscriptionUseCase
import com.myopenclaw.domain.usecase.marketdata.GetInsiderTradingUseCase
import com.myopenclaw.domain.usecase.marketdata.GetInsiderTradingStatsUseCase
import com.myopenclaw.domain.usecase.marketdata.GetCongressTradingUseCase
import com.myopenclaw.domain.usecase.marketdata.GetCongressTradingStatsUseCase
import com.myopenclaw.domain.usecase.marketdata.GetOptionsFlowUseCase
import com.myopenclaw.domain.usecase.marketdata.GetOptionsFlowStatsUseCase
import com.myopenclaw.domain.usecase.marketdata.GetTradeIdeasUseCase
import com.myopenclaw.domain.usecase.marketdata.SearchStocksUseCase
import com.myopenclaw.domain.usecase.marketdata.GetSocialSentimentUseCase
import com.myopenclaw.domain.usecase.marketdata.GetSocialSentimentStatsUseCase
import com.myopenclaw.domain.usecase.marketdata.GetDarkPoolDataUseCase
import com.myopenclaw.domain.usecase.marketdata.GetDarkPoolStatsUseCase
import com.myopenclaw.domain.usecase.marketdata.GetGovernmentContractsUseCase
import com.myopenclaw.domain.usecase.marketdata.GetGovernmentContractsStatsUseCase
import com.myopenclaw.domain.usecase.marketdata.GetLobbyistActivityUseCase
import com.myopenclaw.domain.usecase.marketdata.GetLobbyistActivityStatsUseCase
import com.myopenclaw.domain.usecase.marketdata.GetPoliticalDonationsUseCase
import com.myopenclaw.domain.usecase.marketdata.GetPoliticalDonationsStatsUseCase
import com.myopenclaw.ui.viewmodel.auth.ForgotPasswordViewModel
import com.myopenclaw.ui.viewmodel.auth.OnboardingViewModel
import com.myopenclaw.ui.viewmodel.auth.SignInViewModel
import com.myopenclaw.ui.viewmodel.auth.SignUpViewModel
import com.myopenclaw.ui.viewmodel.subscription.SubscriptionViewModel
import com.myopenclaw.ui.viewmodel.home.HomeViewModel
import com.myopenclaw.ui.viewmodel.marketdata.InsiderTradingViewModel
import com.myopenclaw.ui.viewmodel.marketdata.CongressTradingViewModel
import com.myopenclaw.ui.viewmodel.marketdata.OptionsFlowViewModel
import com.myopenclaw.ui.viewmodel.marketdata.SignalDetailsViewModel
import com.myopenclaw.ui.viewmodel.marketdata.SocialSentimentViewModel
import com.myopenclaw.ui.viewmodel.marketdata.DarkPoolViewModel
import com.myopenclaw.ui.viewmodel.marketdata.GovernmentContractsViewModel
import com.myopenclaw.ui.viewmodel.marketdata.LobbyistActivityViewModel
import com.myopenclaw.ui.viewmodel.marketdata.PoliticalDonationsViewModel
import com.myopenclaw.ui.viewmodel.markets.MarketsViewModel
import com.myopenclaw.ui.viewmodel.search.SearchViewModel
import com.myopenclaw.ui.viewmodel.ai.TradeIdeasViewModel
import com.myopenclaw.ui.viewmodel.ai.ChartAnalysisViewModel
import com.myopenclaw.ui.viewmodel.alerts.AlertViewModel
import com.myopenclaw.data.local.AlertStore
import com.myopenclaw.ui.viewmodel.ai.ChatViewModel
import com.myopenclaw.domain.usecase.ai.AnalyzeChartUseCase
import com.myopenclaw.domain.usecase.ai.SendChatMessageUseCase
import com.myopenclaw.data.repository.UserRepositoryImpl
import com.myopenclaw.domain.repository.UserRepository
import com.myopenclaw.ui.viewmodel.profile.ProfileViewModel
import com.myopenclaw.data.repository.SignalsRepositoryImpl
import com.myopenclaw.domain.repository.SignalsRepository
import com.myopenclaw.domain.usecase.signals.CheckMarketSignalAccessUseCase
import com.myopenclaw.ui.viewmodel.signals.SignalsViewModel
import com.myopenclaw.data.repository.AnalysisRepositoryImpl
import com.myopenclaw.domain.repository.AnalysisRepository
import com.myopenclaw.ui.viewmodel.analysis.AnalysisHistoryViewModel
import com.myopenclaw.data.repository.WatchlistRepositoryImpl
import com.myopenclaw.domain.repository.WatchlistRepository
import com.myopenclaw.ui.viewmodel.watchlist.WatchlistViewModel
import com.myopenclaw.data.repository.RevenueCatRepositoryImpl
import com.myopenclaw.domain.repository.RevenueCatRepository
import com.myopenclaw.ui.viewmodel.subscription.RevenueCatViewModel
import com.myopenclaw.data.local.cache.CacheManager
import com.myopenclaw.data.local.cache.CacheManagerImpl
import com.myopenclaw.data.local.cache.DatabaseDriverFactory
import com.myopenclaw.data.local.cache.MyOpenClawDatabase
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val networkModule = module {
    single<HttpClient> {
        HttpClientFactory.create(getAuthToken = { get<AuthManager>().getIdToken() })
    }

    single<ApiService> {
        ApiService(httpClient = get())
    }
}

val authModule = module {
    // AuthManager is provided by platformModule()

    single<AuthRepository> {
        AuthRepositoryImpl(authManager = get())
    }

    // SessionManager for persistent login
    single {
        SessionManager(
            authManager = get(),
            preferencesManager = get()
        )
    }

    factory { SignUpUseCase(authRepository = get()) }
    factory { SignInUseCase(authRepository = get()) }
    factory { SignOutUseCase(authRepository = get()) }
    factory { ResetPasswordUseCase(authRepository = get()) }

    viewModel {
        SignUpViewModel(
            signUpUseCase = get(),
            authRepository = get(),
            googleSignInProvider = get(),
            appleSignInProvider = get(),
            preferencesManager = get(),
            sessionManager = get()
        )
    }
    viewModel {
        SignInViewModel(
            signInUseCase = get(),
            authRepository = get(),
            googleSignInProvider = get(),
            appleSignInProvider = get(),
            preferencesManager = get(),
            sessionManager = get()
        )
    }
    viewModel { ForgotPasswordViewModel(resetPasswordUseCase = get()) }

    // OnboardingViewModel needs to be shared across multiple onboarding screens
    // so we use 'single' instead of 'viewModel' to maintain state during the flow
    single { OnboardingViewModel(preferencesManager = get(), userRepository = get()) }
}

val subscriptionModule = module {
    single<SubscriptionRepository> {
        SubscriptionRepositoryImpl(apiService = get())
    }

    factory { GetSubscriptionUseCase(subscriptionRepository = get()) }
    factory { CreateCheckoutSessionUseCase(subscriptionRepository = get()) }

    viewModel {
        SubscriptionViewModel(
            getSubscriptionUseCase = get(),
            createCheckoutSessionUseCase = get()
        )
    }
}

val marketDataModule = module {
    single<MarketDataRepository> {
        MarketDataRepositoryImpl(
            apiService = get(),
            cacheManager = get()
        )
    }

    // Market Data Use Cases
    factory { GetInsiderTradingUseCase(marketDataRepository = get()) }
    factory { GetInsiderTradingStatsUseCase(marketDataRepository = get()) }
    factory { GetCongressTradingUseCase(marketDataRepository = get()) }
    factory { GetCongressTradingStatsUseCase(marketDataRepository = get()) }
    factory { GetOptionsFlowUseCase(marketDataRepository = get()) }
    factory { GetOptionsFlowStatsUseCase(marketDataRepository = get()) }
    factory { GetTradeIdeasUseCase(marketDataRepository = get()) }
    factory { SearchStocksUseCase(marketDataRepository = get()) }

    // Sprint 5: Additional Market Intelligence Use Cases
    factory { GetSocialSentimentUseCase(marketDataRepository = get()) }
    factory { GetSocialSentimentStatsUseCase(marketDataRepository = get()) }
    factory { GetDarkPoolDataUseCase(marketDataRepository = get()) }
    factory { GetDarkPoolStatsUseCase(marketDataRepository = get()) }
    factory { GetGovernmentContractsUseCase(marketDataRepository = get()) }
    factory { GetGovernmentContractsStatsUseCase(marketDataRepository = get()) }
    factory { GetLobbyistActivityUseCase(marketDataRepository = get()) }
    factory { GetLobbyistActivityStatsUseCase(marketDataRepository = get()) }
    factory { GetPoliticalDonationsUseCase(marketDataRepository = get()) }
    factory { GetPoliticalDonationsStatsUseCase(marketDataRepository = get()) }

    // Market Data ViewModels
    viewModel {
        HomeViewModel(
            getInsiderTradingUseCase = get(),
            getCongressTradingUseCase = get(),
            getOptionsFlowUseCase = get(),
            getTradeIdeasUseCase = get()
        )
    }

    viewModel {
        InsiderTradingViewModel(
            getInsiderTradingUseCase = get(),
            getInsiderTradingStatsUseCase = get()
        )
    }

    viewModel {
        CongressTradingViewModel(
            getCongressTradingUseCase = get(),
            getCongressTradingStatsUseCase = get()
        )
    }

    viewModel {
        OptionsFlowViewModel(
            getOptionsFlowUseCase = get(),
            getOptionsFlowStatsUseCase = get()
        )
    }

    viewModel {
        SearchViewModel(
            searchStocksUseCase = get()
        )
    }

    viewModel {
        SignalDetailsViewModel(
            getInsiderTradingUseCase = get(),
            watchlistRepository = get(),
            shareHandler = get()
        )
    }

    // AI Features Use Cases
    factory { AnalyzeChartUseCase(marketDataRepository = get()) }
    factory { SendChatMessageUseCase(marketDataRepository = get()) }

    // AI Features ViewModels
    viewModel {
        TradeIdeasViewModel(
            getTradeIdeasUseCase = get()
        )
    }

    viewModel {
        ChartAnalysisViewModel(
            analyzeChartUseCase = get(),
            revenueCatRepository = get(),
            preferencesManager = get(),
            cacheManager = getOrNull()
        )
    }

    viewModel {
        ChatViewModel(
            sendChatMessageUseCase = get(),
            preferencesManager = get(),
            revenueCatRepository = get()
        )
    }

    // Sprint 5: Additional Market Intelligence ViewModels
    viewModel {
        SocialSentimentViewModel(
            getSocialSentimentUseCase = get(),
            getSocialSentimentStatsUseCase = get()
        )
    }

    viewModel {
        DarkPoolViewModel(
            getDarkPoolDataUseCase = get(),
            getDarkPoolStatsUseCase = get()
        )
    }

    viewModel {
        GovernmentContractsViewModel(
            getGovernmentContractsUseCase = get(),
            getGovernmentContractsStatsUseCase = get()
        )
    }

    viewModel {
        LobbyistActivityViewModel(
            getLobbyistActivityUseCase = get(),
            getLobbyistActivityStatsUseCase = get()
        )
    }

    viewModel {
        PoliticalDonationsViewModel(
            getPoliticalDonationsUseCase = get(),
            getPoliticalDonationsStatsUseCase = get()
        )
    }

    // Markets Screen ViewModel - aggregates all market data
    viewModel {
        MarketsViewModel(
            getInsiderTradingUseCase = get(),
            getCongressTradingUseCase = get(),
            getOptionsFlowUseCase = get(),
            getSocialSentimentUseCase = get(),
            getDarkPoolDataUseCase = get(),
            getGovernmentContractsUseCase = get(),
            getLobbyistActivityUseCase = get(),
            getPoliticalDonationsUseCase = get(),
            httpClient = get()
        )
    }
}

val profileModule = module {
    single<UserRepository> {
        UserRepositoryImpl(apiService = get())
    }

    viewModel {
        ProfileViewModel(
            userRepository = get(),
            authRepository = get(),
            preferencesManager = get(),
            sessionManager = get(),
            revenueCatRepository = getOrNull(),
            cacheManager = getOrNull()
        )
    }
}

val signalsModule = module {
    single<SignalsRepository> {
        SignalsRepositoryImpl(
            apiService = get(),
            cacheManager = get()
        )
    }

    factory {
        CheckMarketSignalAccessUseCase(
            preferencesManager = get(),
            revenueCatRepository = get()
        )
    }

    viewModel {
        SignalsViewModel(
            signalsRepository = get(),
            checkMarketSignalAccessUseCase = get()
        )
    }
}

val analysisModule = module {
    single<AnalysisRepository> {
        AnalysisRepositoryImpl(
            apiService = get(),
            cacheManager = get()
        )
    }

    viewModel {
        AnalysisHistoryViewModel(analysisRepository = get())
    }
}

val watchlistModule = module {
    single<WatchlistRepository> {
        WatchlistRepositoryImpl(preferencesManager = get())
    }

    single<com.myopenclaw.domain.repository.StockSearchRepository> {
        com.myopenclaw.data.repository.StockSearchRepositoryImpl(httpClient = get())
    }

    viewModel {
        WatchlistViewModel(watchlistRepository = get())
    }

    viewModel {
        com.myopenclaw.ui.viewmodel.watchlist.StockSearchViewModel(stockSearchRepository = get())
    }
}

val revenueCatModule = module {
    single<RevenueCatRepository> {
        RevenueCatRepositoryImpl(preferencesManager = get())
    }

    viewModel {
        RevenueCatViewModel(revenueCatRepository = get())
    }

    // Alert system
    single { AlertStore(preferencesManager = get()) }

    viewModel {
        AlertViewModel(alertStore = get())
    }
}

/**
 * Cache module for local data caching using SQLDelight.
 * Provides MyOpenClawDatabase and CacheManager for offline-first data access.
 */
val cacheModule = module {
    // SQLDelight Database instance
    // DatabaseDriverFactory is provided by platformModule()
    single {
        MyOpenClawDatabase(get<DatabaseDriverFactory>().createDriver())
    }

    // CacheManager for managing cached data
    single<CacheManager> {
        CacheManagerImpl(database = get())
    }
}

/**
 * All common Koin modules.
 * Note: platformModule() must be added first as it provides AuthManager, PreferencesManager, and DatabaseDriverFactory
 * Note: cacheModule must be loaded early as repositories may depend on CacheManager
 * Note: revenueCatModule must be loaded before profileModule so RevenueCatRepository is available for injection
 */
fun appModules() = listOf(
    platformModule(),  // Platform-specific dependencies (AuthManager, PreferencesManager, DatabaseDriverFactory)
    cacheModule,       // Cache module - must be after platformModule for DatabaseDriverFactory
    networkModule,
    authModule,
    subscriptionModule,
    marketDataModule,
    revenueCatModule,  // RevenueCat SDK for in-app subscriptions - must be before profileModule
    profileModule,
    signalsModule,
    analysisModule,
    watchlistModule
)
