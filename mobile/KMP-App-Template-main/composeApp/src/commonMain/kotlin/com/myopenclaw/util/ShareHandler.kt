package com.myopenclaw.util

/**
 * Platform-specific share handler for sharing text content
 */
expect class ShareHandler {
    /**
     * Share text content using the system share sheet
     * @param text The text to share
     * @param title Optional title for the share sheet
     */
    fun shareText(text: String, title: String? = null)
}
