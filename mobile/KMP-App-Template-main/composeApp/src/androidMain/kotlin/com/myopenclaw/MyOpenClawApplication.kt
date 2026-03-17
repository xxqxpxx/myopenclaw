package com.myopenclaw

import android.app.Application
import com.myopenclaw.data.remote.RevenueCatConfig
import com.myopenclaw.di.appModules
import com.myopenclaw.util.initializePlatformUtils
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Custom Application class for myOpenClaw
 * Initializes Koin dependency injection with Android context
 */
class MyOpenClawApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize PlatformUtils
        initializePlatformUtils(this)

        // Initialize RevenueCat
        RevenueCatConfig.configure()

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
