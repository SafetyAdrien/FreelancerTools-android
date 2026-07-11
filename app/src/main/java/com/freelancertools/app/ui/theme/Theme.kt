package com.freelancertools.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = AccentRed,
    onPrimary = Color.White,
    secondary = InfoBlue,
    onSecondary = Color.White,
    tertiary = AccentPurple,
    background = BackgroundDark,
    onBackground = Color.White,
    surface = SurfaceDark,
    onSurface = Color.White,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = DividerDark,
    error = Color(0xFFE74C3C),
)

private val LightColors = lightColorScheme(
    primary = AccentRed,
    onPrimary = Color.White,
    secondary = InfoBlue,
    onSecondary = Color.White,
    tertiary = AccentPurple,
    background = BackgroundLight,
    onBackground = Color(0xFF141414),
    surface = SurfaceLight,
    onSurface = Color(0xFF141414),
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = DividerLight,
    error = Color(0xFFD32F2F),
)

enum class AppThemeMode { SYSTEM, DARK, LIGHT }

@Composable
fun FreelancerToolsTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    content: @Composable () -> Unit,
) {
    val useDark = when (themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
    }
    val colors = if (useDark) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
