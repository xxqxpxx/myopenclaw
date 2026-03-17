package com.myopenclaw.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryVariant,
    secondary = Secondary,
    onSecondary = OnSecondary,
    background = BackgroundDark,
    onBackground = OnBackground,
    surface = SurfaceDark,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    error = Error,
    onError = OnPrimary,
    outline = Border
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryVariant,
    secondary = Secondary,
    onSecondary = OnSecondary,
    background = BackgroundLight,
    onBackground = Color(0xFF000000),
    surface = SurfaceLight,
    onSurface = Color(0xFF000000),
    error = Error,
    onError = Color(0xFFFFFFFF),
    outline = Border
)

/**
 * Main app theme - works with both my openClaw and Profit AI Pro variants.
 * Colors are defined in Color.kt and can be overridden per flavor using BrandConfig.
 */
@Composable
fun MyOpenClawTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

/**
 * Alias for the app theme that works regardless of flavor.
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) = MyOpenClawTheme(darkTheme = darkTheme, content = content)
