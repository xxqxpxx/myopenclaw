package com.myopenclaw.config

import androidx.compose.ui.graphics.Color

/**
 * Brand configuration for Profit AI Pro variant
 * Uses same colors as my openClaw, only app name differs
 */
object BrandConfig {
    const val APP_NAME = "Profit AI Pro"
    const val FLAVOR = "profitai"

    // Primary Colors - Green/Teal Accent
    val Primary = Color(0xFF00D9A3)
    val PrimaryVariant = Color(0xFF00B890)
    val OnPrimary = Color(0xFF000000)

    // Secondary Colors
    val Secondary = Color(0xFF03DAC6)
    val OnSecondary = Color(0xFF000000)

    // Background Colors - Dark Theme
    val BackgroundDark = Color(0xFF0D1023)
    val SurfaceDark = Color(0xFF131B27)
    val SurfaceVariant = Color(0xFF1C2635)

    // Semantic Colors
    val Success = Color(0xFF00E676)
    val Error = Color(0xFFFF5252)
    val Warning = Color(0xFFFFB020)

    // Premium Colors
    val PremiumGold = Color(0xFFFFD700)
    val PremiumBadge = Color(0xFFFFAB00)
}
