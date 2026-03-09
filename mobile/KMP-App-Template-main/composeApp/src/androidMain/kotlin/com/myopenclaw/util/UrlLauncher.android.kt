package com.myopenclaw.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

/**
 * Android implementation of URL launcher
 */
actual class UrlLauncher(private val context: Context) {

    companion object {
        // App package name for Play Store
        private const val APP_PACKAGE = "com.myopenclaw"
    }

    actual fun openUrl(url: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    actual fun openUrlInApp(url: String): Boolean {
        return try {
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()

            customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            customTabsIntent.launchUrl(context, Uri.parse(url))
            true
        } catch (e: Exception) {
            // Fallback to system browser
            openUrl(url)
        }
    }

    actual fun openEmail(email: String, subject: String?, body: String?): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
                body?.let { putExtra(Intent.EXTRA_TEXT, it) }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    actual fun openAppStore(): Boolean {
        return try {
            // Try to open in Play Store app first
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$APP_PACKAGE")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            // Fallback to browser
            openUrl("https://play.google.com/store/apps/details?id=$APP_PACKAGE")
        }
    }
}
