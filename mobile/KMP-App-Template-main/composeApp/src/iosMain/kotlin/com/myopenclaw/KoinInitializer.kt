package com.myopenclaw

import com.myopenclaw.di.appModules
import org.koin.core.context.startKoin

/**
 * Initializes Koin for iOS
 * This is called from Swift code in iOSApp.swift
 */
fun doInitKoin() {
    startKoin {
        modules(appModules())
    }
}
