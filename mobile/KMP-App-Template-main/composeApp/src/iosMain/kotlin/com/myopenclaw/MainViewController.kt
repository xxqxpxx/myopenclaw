package com.myopenclaw

import androidx.compose.ui.window.ComposeUIViewController
import com.myopenclaw.data.remote.RevenueCatConfig

fun MainViewController() = ComposeUIViewController { App() }

/**
 * Initialize RevenueCat SDK for iOS
 * This should be called from the iOS app's AppDelegate or SwiftUI App init
 */
fun initializeRevenueCat() {
    RevenueCatConfig.configure()
}
