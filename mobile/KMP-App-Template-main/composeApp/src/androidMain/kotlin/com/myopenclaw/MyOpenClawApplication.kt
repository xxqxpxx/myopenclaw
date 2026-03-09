package com.myopenclaw

import android.app.Application
import com.myopenclaw.data.remote.RevenueCatConfig
import com.myopenclaw.di.appModules
import com.myopenclaw.util.initializePlatformUtils
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Custom Application class for SignalWhisper
 * Initializes Firebase and Koin dependency injection with Android context
 */
class MyOpenClawApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize PlatformUtils
        initializePlatformUtils(this)

        // Initialize RevenueCat
        RevenueCatConfig.configure()

        // Initialize Firebase
        // The Firebase SDK needs to be initialized before any Firebase APIs are used
        // Configuration is automatically loaded from google-services.json via the gradle plugin
        // Note: FirebaseInitProvider may have already initialized Firebase, so we check for that
        try {
            Firebase.initialize(
                context = this,
                options = FirebaseOptions(
                    applicationId = "1:733674476755:android:0f5917152c9f2c44073026",
                    apiKey = "AIzaSyDaaXXs9SIxTCu_2xWppHZmi3QqgZtWRKM",
                    projectId = "trumppulse-lur14",
                    gcmSenderId = "733674476755"
                )
            )
            // // println("MyOpenClawApplication: Firebase initialized successfully")
        } catch (e: IllegalStateException) {
            // Firebase may already be initialized by FirebaseInitProvider
            // This is expected and harmless - FirebaseInitProvider runs before Application.onCreate()
            if (e.message?.contains("already exists", ignoreCase = true) == true) {
                // // println("MyOpenClawApplication: Firebase already initialized by FirebaseInitProvider")
            } else {
                // // println("MyOpenClawApplication: Firebase initialization error - ${e.message}")
                e.printStackTrace()
            }
        } catch (e: Exception) {
            // // println("MyOpenClawApplication: Firebase initialization error - ${e.message}")
            e.printStackTrace()
        }

        // Initialize Koin
        startKoin {
            // Log Koin into Android logger
            androidLogger(Level.ERROR)
            // Reference Android context
            androidContext(this@MyOpenClawApplication)
            // Load modules
            modules(appModules())
        }
    }
}
