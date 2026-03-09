package com.myopenclaw.di

import com.myopenclaw.data.auth.AuthManager
import com.myopenclaw.data.auth.GoogleSignInProvider
import com.myopenclaw.data.auth.AppleSignInProvider
import com.myopenclaw.data.local.PreferencesManager
import org.koin.core.module.Module

/**
 * Platform-specific module for dependency injection
 * Each platform (Android/iOS) provides its own implementation
 */
expect fun platformModule(): Module

/**
 * Configuration for Firebase Auth
 */
object FirebaseConfig {
    // Google Web Client ID from Firebase Console (TrumpPulse project)
    // This should be set from your google-services.json (Android) or GoogleService-Info.plist (iOS)
    var webClientId: String = "733674476755-ghltrlm3ccftoig5bpsmu70mkcf79i59.apps.googleusercontent.com"

    // Whether to use mock auth for development
    var useMockAuth: Boolean = false
}
