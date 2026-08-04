package com.codeforces.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = CodeforcesRed,
    onPrimary = CfTextPrimary,
    primaryContainer = CfRedDark,
    onPrimaryContainer = CfTextPrimary,
    secondary = CfRedLight,
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
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = CfBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
