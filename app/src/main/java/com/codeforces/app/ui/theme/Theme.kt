package com.codeforces.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = CodeforcesAccent,
    onPrimary = CfTextPrimary,
    primaryContainer = CfAccentDark,
    onPrimaryContainer = CfTextPrimary,
    secondary = CfAccentLight,
    onSecondary = CfTextPrimary,
    background = CfBackground,
    onBackground = CfTextPrimary,
    surface = CfSurface,
    onSurface = CfTextPrimary,
    surfaceVariant = CfCardSurface,
    onSurfaceVariant = CfTextSecondary,
    outline = CfDivider,
    error = VerdictWA
)

@Composable
fun CodeforcesTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = CfBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AppTypography,
        content = content
    )
}
