package com.myopenclaw.util

/**
 * Platform-specific URL launcher for opening external URLs
 * Used for Stripe checkout, external links, etc.
 */
expect class UrlLauncher {
    /**
     * Opens a URL in the system browser
     * @param url The URL to open
     * @return true if the URL was opened successfully
     */
    fun openUrl(url: String): Boolean

    /**
     * Opens a URL in an in-app browser (Custom Tab on Android, Safari View on iOS)
     * Falls back to system browser if not available
     * @param url The URL to open
     * @return true if the URL was opened successfully
     */
    fun openUrlInApp(url: String): Boolean

    /**
     * Opens the email client with pre-filled recipient
     * @param email The recipient email address
     * @param subject Optional email subject
     * @param body Optional email body
     * @return true if the email client was opened successfully
     */
    fun openEmail(email: String, subject: String? = null, body: String? = null): Boolean

    /**
     * Opens the app's page in the app store for rating
     * @return true if the store was opened successfully
     */
    fun openAppStore(): Boolean
}
