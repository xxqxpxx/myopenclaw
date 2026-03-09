package com.myopenclaw.util

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Android implementation of PlatformUtils.
 */
class AndroidPlatformUtils(private val context: Context) : PlatformUtils {

    override fun shareText(text: String, subject: String?) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            if (subject != null) {
                putExtra(Intent.EXTRA_SUBJECT, subject)
            }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, "Share via")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    override fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // // println("Error opening URL: ${e.message}")
        }
    }

    override fun rateApp() {
        val packageName = context.packageName
        try {
            // Try to open in Play Store app
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fall back to browser
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    override fun openEmail(email: String, subject: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // // println("Error opening email: ${e.message}")
        }
    }
}

/**
 * Global context reference for PlatformUtils.
 */
private var applicationContext: Context? = null

/**
 * Initialize the Android PlatformUtils with application context.
 * Should be called from Application.onCreate()
 */
fun initializePlatformUtils(context: Context) {
    applicationContext = context.applicationContext
}

/**
 * Provides the Android-specific PlatformUtils implementation.
 */
actual fun getPlatformUtils(): PlatformUtils {
    val context = applicationContext
        ?: throw IllegalStateException(
            "PlatformUtils not initialized. Call initializePlatformUtils() from Application.onCreate()"
        )
    return AndroidPlatformUtils(context)
}

actual val isIOSPlatform: Boolean = false
