package com.myopenclaw.util

/**
 * Platform-specific utilities for sharing, opening URLs, and rating the app.
 */
interface PlatformUtils {
    /**
     * Share text content using the platform's native share sheet.
     *
     * @param text The text to share
     * @param subject Optional subject/title for the share (used on some platforms)
     */
    fun shareText(text: String, subject: String? = null)

    /**
     * Open a URL in the default browser.
     *
     * @param url The URL to open
     */
    fun openUrl(url: String)

    /**
     * Open the app's page in the platform's app store for rating.
     */
    fun rateApp()

    /**
     * Open an email client with pre-filled recipient and subject.
     *
     * @param email The recipient email address
     * @param subject The email subject
     */
    fun openEmail(email: String, subject: String)
}

/**
 * Get the platform-specific implementation of PlatformUtils.
 */
expect fun getPlatformUtils(): PlatformUtils

/**
 * Whether the current platform is iOS.
 */
expect val isIOSPlatform: Boolean
