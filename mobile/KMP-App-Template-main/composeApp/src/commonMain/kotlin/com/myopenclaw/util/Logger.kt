package com.myopenclaw.util

/**
 * Simple logger utility that only prints in debug builds.
 * In production, all debug logging is suppressed.
 */
object AppLogger {
    /**
     * Set to false for production builds.
     * TODO: Wire to BuildConfig.DEBUG when available in KMP.
     */
    var isDebugEnabled: Boolean = false

    fun d(tag: String, msg: String) {
        if (isDebugEnabled) {
            println("[$tag] $msg")
        }
    }

    fun e(tag: String, msg: String, throwable: Throwable? = null) {
        // Always log errors
        println("[$tag] ERROR: $msg")
        throwable?.printStackTrace()
    }

    fun i(tag: String, msg: String) {
        if (isDebugEnabled) {
            println("[$tag] $msg")
        }
    }
}
