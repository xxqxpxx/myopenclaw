package com.myopenclaw.util

import platform.Foundation.setValue
import platform.Foundation.valueForKey
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIModalPresentationPageSheet
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene

/**
 * iOS implementation of share handler
 */
actual class ShareHandler {

    actual fun shareText(text: String, title: String?) {
        val activityItems = listOf(text)
        val activityVC = UIActivityViewController(
            activityItems = activityItems,
            applicationActivities = null
        )

        // Get the key window to present from
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

        topController?.let { controller ->
            // Configure popover for iPad using KVC
            try {
                val popover = activityVC.valueForKey("popoverPresentationController")
                if (popover != null) {
                    (popover as platform.darwin.NSObject).setValue(
                        controller.view,
                        forKey = "sourceView"
                    )
                }
            } catch (_: Exception) {
                // Fallback
            }

            activityVC.setModalPresentationStyle(UIModalPresentationPageSheet)
            controller.presentViewController(activityVC, animated = true, completion = null)
        }
    }
}
