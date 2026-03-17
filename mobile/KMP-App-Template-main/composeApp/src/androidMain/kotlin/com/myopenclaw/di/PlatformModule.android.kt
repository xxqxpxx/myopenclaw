package com.myopenclaw.di

import com.myopenclaw.data.auth.AuthManager
import com.myopenclaw.data.auth.SupabaseAuthManager
import com.myopenclaw.data.auth.MockAuthManager
import com.myopenclaw.data.auth.GoogleSignInProvider
import com.myopenclaw.data.auth.AppleSignInProvider
import com.myopenclaw.data.local.AndroidPreferencesManager
import com.myopenclaw.data.local.PreferencesManager
import com.myopenclaw.data.local.cache.DatabaseDriverFactory
import com.myopenclaw.util.ImagePicker
import com.myopenclaw.util.ShareHandler
import com.myopenclaw.util.UrlLauncher
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android-specific Koin module for platform dependencies
 */
actual fun platformModule(): Module = module {
    // Preferences Manager using SharedPreferences
    single<PreferencesManager> {
        AndroidPreferencesManager(androidContext())
    }

    // Database Driver Factory for SQLDelight cache
    single {
        DatabaseDriverFactory(androidContext())
    }

    // Supabase Auth Manager
    single<AuthManager> {
        if (SupabaseConfig.useMockAuth) {
            MockAuthManager()
        } else {
            SupabaseAuthManager()
        }
    }

    // Google Sign-In Provider
    factory {
        GoogleSignInProvider(
            context = androidContext(),
            webClientId = SupabaseConfig.webClientId
        )
    }

    // Apple Sign-In Provider (not available on Android)
    factory {
        AppleSignInProvider()
    }

    // URL Launcher for opening external URLs (Stripe checkout, etc.)
    factory {
        UrlLauncher(androidContext())
    }

    // Share Handler for sharing content
    factory {
        ShareHandler(androidContext())
    }

    // Image Picker for camera and gallery access
    // Note: ImagePicker.getInstance() returns the instance initialized by MainActivity
    factory {
        ImagePicker.getInstance() ?: ImagePicker(androidContext())
    }
}
