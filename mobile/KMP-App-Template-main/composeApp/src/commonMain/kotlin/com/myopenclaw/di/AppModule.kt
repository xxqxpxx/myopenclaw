package com.myopenclaw.di

import com.myopenclaw.data.auth.AuthManager
import com.myopenclaw.data.session.SessionManager
import com.myopenclaw.data.remote.ApiService
import com.myopenclaw.data.remote.HttpClientFactory
import com.myopenclaw.data.repository.AuthRepositoryImpl
import com.myopenclaw.data.repository.ConversationRepositoryImpl
import com.myopenclaw.data.repository.SubscriptionRepositoryImpl
import com.myopenclaw.data.repository.UserRepositoryImpl
import com.myopenclaw.data.repository.RevenueCatRepositoryImpl
import com.myopenclaw.domain.repository.AuthRepository
import com.myopenclaw.domain.repository.ConversationRepository
import com.myopenclaw.domain.repository.SubscriptionRepository
import com.myopenclaw.domain.repository.UserRepository
import com.myopenclaw.domain.repository.RevenueCatRepository
import com.myopenclaw.domain.usecase.auth.ResetPasswordUseCase
import com.myopenclaw.domain.usecase.auth.SignInUseCase
import com.myopenclaw.domain.usecase.auth.SignOutUseCase
import com.myopenclaw.domain.usecase.auth.SignUpUseCase
import com.myopenclaw.domain.usecase.subscription.CreateCheckoutSessionUseCase
import com.myopenclaw.domain.usecase.subscription.GetSubscriptionUseCase
import com.myopenclaw.ui.viewmodel.auth.ForgotPasswordViewModel
import com.myopenclaw.ui.viewmodel.auth.OnboardingViewModel
import com.myopenclaw.ui.viewmodel.auth.SignInViewModel
import com.myopenclaw.ui.viewmodel.auth.SignUpViewModel
import com.myopenclaw.ui.viewmodel.subscription.SubscriptionViewModel
import com.myopenclaw.ui.viewmodel.subscription.RevenueCatViewModel
import com.myopenclaw.ui.viewmodel.home.HomeViewModel
import com.myopenclaw.ui.viewmodel.profile.ProfileViewModel
import com.myopenclaw.ui.viewmodel.ai.ChatViewModel
import com.myopenclaw.ui.viewmodel.ai.ConversationListViewModel
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
    single<AuthRepository> {
        AuthRepositoryImpl(authManager = get())
    }

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

val chatModule = module {
    single<ConversationRepository> {
        ConversationRepositoryImpl(apiService = get())
    }

    viewModel {
        ChatViewModel(conversationRepository = get())
    }

    viewModel {
        ConversationListViewModel(conversationRepository = get())
    }

    viewModel {
        HomeViewModel()
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

val revenueCatModule = module {
    single<RevenueCatRepository> {
        RevenueCatRepositoryImpl(preferencesManager = get())
    }

    viewModel {
        RevenueCatViewModel(revenueCatRepository = get())
    }
}

val cacheModule = module {
    single {
        MyOpenClawDatabase(get<DatabaseDriverFactory>().createDriver())
    }

    single<CacheManager> {
        CacheManagerImpl(database = get())
    }
}

fun appModules() = listOf(
    platformModule(),
    cacheModule,
    networkModule,
    authModule,
    subscriptionModule,
    chatModule,
    revenueCatModule,
    profileModule
)
