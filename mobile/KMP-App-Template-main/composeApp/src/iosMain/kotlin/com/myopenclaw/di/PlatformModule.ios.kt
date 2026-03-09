package com.myopenclaw.di

import com.myopenclaw.data.auth.AuthManager
import com.myopenclaw.data.auth.FirebaseAuthManager
import com.myopenclaw.data.auth.MockAuthManager
import com.myopenclaw.data.auth.GoogleSignInProvider
import com.myopenclaw.data.auth.AppleSignInProvider
import com.myopenclaw.data.local.IOSPreferencesManager
import com.myopenclaw.data.local.PreferencesManager
import com.myopenclaw.data.local.cache.DatabaseDriverFactory
import com.myopenclaw.util.ImagePicker
import com.myopenclaw.util.ShareHandler
import com.myopenclaw.util.UrlLauncher
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * iOS-specific Koin module for platform dependencies
 */
actual fun platformModule(): Module = module {
    // Preferences Manager using NSUserDefaults
    single<PreferencesManager> {
        IOSPreferencesManager()
    }

    // Database Driver Factory for SQLDelight cache
    single {
        DatabaseDriverFactory()
    }

    // Firebase Auth Manager
    single<AuthManager> {
        if (FirebaseConfig.useMockAuth) {
            MockAuthManager()
        } else {
            FirebaseAuthManager()
        }
    }

    // Google Sign-In Provider
    factory {
        GoogleSignInProvider()
    }

    // Apple Sign-In Provider
    factory {
        AppleSignInProvider()
    }

    // URL Launcher for opening external URLs (Stripe checkout, etc.)
    factory {
        UrlLauncher()
    }

    // Share Handler for sharing content
    factory {
        ShareHandler()
    }

    // Image Picker for camera and gallery access
    factory {
        ImagePicker()
    }
}
