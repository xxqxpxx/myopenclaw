package com.myopenclaw.util

import platform.Foundation.NSURL
import platform.Foundation.setValue
import platform.Foundation.valueForKey
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIModalPresentationPageSheet
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene

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
private const val APP_STORE_ID = "REPLACE_WITH_APP_STORE_ID"

/**
 * Helper to get the topmost presented view controller.
 */
private fun getTopViewController(): UIViewController? {
    val scenes = UIApplication.sharedApplication.connectedScenes
    val windowScene = scenes.firstOrNull { scene ->
        scene is UIWindowScene
    } as? UIWindowScene

    val keyWindow = windowScene?.windows?.firstOrNull { window ->
        (window as? UIWindow)?.isKeyWindow() == true
    } as? UIWindow

    var topController = keyWindow?.rootViewController
    while (topController?.presentedViewController != null) {
        topController = topController.presentedViewController
    }
    return topController
}

/**
 * Configure popover source for iPad using KVC since K/N doesn't expose
 * popoverPresentationController properties directly.
 */
private fun configurePopoverForIPad(
    viewController: UIActivityViewController,
    sourceController: UIViewController
) {
    try {
        val popover = viewController.valueForKey("popoverPresentationController")
        if (popover != null) {
            (popover as platform.darwin.NSObject).setValue(
                sourceController.view,
                forKey = "sourceView"
            )
        }
    } catch (_: Exception) {
        // Fallback: page sheet presentation style handles it
    }
}

/**
 * Helper to open a URL using the non-deprecated API.
 */
private fun openUrlSafely(url: String) {
    val nsUrl = NSURL.URLWithString(url) ?: return
    UIApplication.sharedApplication.openURL(
        nsUrl,
        options = emptyMap<Any?, Any>(),
        completionHandler = null
    )
}

/**
 * iOS implementation of PlatformUtils.
 */
class IOSPlatformUtils : PlatformUtils {

    override fun shareText(text: String, subject: String?) {
        val items = if (subject != null) {
            listOf(subject, text)
        } else {
            listOf(text)
        }

        val activityViewController = UIActivityViewController(
            activityItems = items,
            applicationActivities = null
        )

        val topController = getTopViewController() ?: return

        // Configure popover for iPad (required for UIActivityViewController)
        configurePopoverForIPad(activityViewController, topController)
        activityViewController.setModalPresentationStyle(UIModalPresentationPageSheet)

        topController.presentViewController(
            activityViewController,
            animated = true,
            completion = null
        )
    }

    override fun openUrl(url: String) {
        openUrlSafely(url)
    }

    override fun rateApp() {
        val appId = APP_STORE_ID
        val url = "https://apps.apple.com/app/id$appId?action=write-review"
        openUrlSafely(url)
    }

    override fun openEmail(email: String, subject: String) {
        val encodedSubject = subject.replace(" ", "%20")
        val mailUrl = "mailto:$email?subject=$encodedSubject"
        openUrlSafely(mailUrl)
    }
}

/**
 * Provides the iOS-specific PlatformUtils implementation.
 */
actual fun getPlatformUtils(): PlatformUtils {
    return IOSPlatformUtils()
}

actual val isIOSPlatform: Boolean = true
