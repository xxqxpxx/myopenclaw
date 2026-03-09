package com.myopenclaw.util

import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.SafariServices.SFSafariViewController
import platform.UIKit.UIWindow
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * iOS implementation of URL launcher
 */
@OptIn(ExperimentalForeignApi::class)
actual class UrlLauncher {

    companion object {
        /**
         * App Store ID for the iOS app
         *
         * IMPORTANT: Update this value before App Store release!
         *
         * Steps to find your App Store ID:
         * 1. Go to App Store Connect (https://appstoreconnect.apple.com)
         * 2. Select your app
         * 3. Go to "App Information" section
         * 4. Copy the "Apple ID" (numeric value like 1234567890)
         */
        private const val APP_ID = "REPLACE_WITH_APP_STORE_ID"
    }

    actual fun openUrl(url: String): Boolean {
        val nsUrl = NSURL.URLWithString(url) ?: return false
        return UIApplication.sharedApplication.openURL(nsUrl)
    }

    actual fun openUrlInApp(url: String): Boolean {
        val nsUrl = NSURL.URLWithString(url) ?: return false

        return try {
            val safariVC = SFSafariViewController(nsUrl)

            // Get the key window to present from
            val scenes = UIApplication.sharedApplication.connectedScenes
            val windowScene = scenes.firstOrNull { scene ->
                scene is platform.UIKit.UIWindowScene
            } as? platform.UIKit.UIWindowScene

            val keyWindow = windowScene?.windows?.firstOrNull { window ->
                (window as? UIWindow)?.isKeyWindow() == true
            } as? UIWindow

            val rootViewController = keyWindow?.rootViewController

            if (rootViewController != null) {
                rootViewController.presentViewController(safariVC, animated = true, completion = null)
                true
            } else {
                // Fallback to opening in system browser
                openUrl(url)
            }
        } catch (e: Exception) {
            // Fallback to system browser
            openUrl(url)
        }
    }

    actual fun openEmail(email: String, subject: String?, body: String?): Boolean {
        val mailtoUrl = buildString {
            append("mailto:$email")
            val params = mutableListOf<String>()
            subject?.let { params.add("subject=${it.encodeUrl()}") }
            body?.let { params.add("body=${it.encodeUrl()}") }
            if (params.isNotEmpty()) {
                append("?${params.joinToString("&")}")
            }
        }
        return openUrl(mailtoUrl)
    }

    actual fun openAppStore(): Boolean {
        // Try to open in App Store app
        val appStoreUrl = "itms-apps://itunes.apple.com/app/id$APP_ID?action=write-review"
        return if (UIApplication.sharedApplication.canOpenURL(NSURL.URLWithString(appStoreUrl)!!)) {
            openUrl(appStoreUrl)
        } else {
            // Fallback to web URL
            openUrl("https://apps.apple.com/app/id$APP_ID")
        }
    }

    private fun String.encodeUrl(): String {
        return this.replace(" ", "%20")
            .replace("&", "%26")
            .replace("=", "%3D")
            .replace("?", "%3F")
            .replace("\n", "%0A")
    }
}
