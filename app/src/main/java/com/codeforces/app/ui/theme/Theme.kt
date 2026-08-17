package com.codeforces.app.ui.theme

import android.app.Activity
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat

@Composable
fun CodeforcesTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    val isDark = CfThemeState.isDark

    // Snap the blend factor to the target theme — the circular reveal overlay
    // (ThemeRevealState) provides the visible transition, so the palette under
    // it must already be final when the reveal sweeps across.
    LaunchedEffect(isDark) {
        CfThemeState.darkProgress = if (isDark) 1f else 0f
    }

    // Built inside composition so the scheme recomputes when the theme flips.
    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = CodeforcesAccent,
            onPrimary = CfBackground,
            primaryContainer = CfAccentDark,
            onPrimaryContainer = CfBackground,
            secondary = CfAccentLight,
            onSecondary = CfBackground,
            background = CfBackground,
            onBackground = CfTextPrimary,
            surface = CfSurface,
            onSurface = CfTextPrimary,
            surfaceVariant = CfCardSurface,
            onSurfaceVariant = CfTextSecondary,
            outline = CfDivider,
            error = VerdictWA
        )
    } else {
        lightColorScheme(
            primary = CodeforcesAccent,
            onPrimary = androidx.compose.ui.graphics.Color.White,
            primaryContainer = CfAccentLight,
            onPrimaryContainer = androidx.compose.ui.graphics.Color.White,
            secondary = CfAccentLight,
            onSecondary = androidx.compose.ui.graphics.Color.White,
            background = CfBackground,
            onBackground = CfTextPrimary,
            surface = CfSurface,
            onSurface = CfTextPrimary,
            surfaceVariant = CfCardSurface,
            onSurfaceVariant = CfTextSecondary,
            outline = CfDivider,
            error = VerdictWA
        )
    }

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = CfBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                !CfThemeState.isDark
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = cfTypography()
    ) {
        // Texts that don't set an explicit color resolve to LocalContentColor —
        // provide the adaptive palette color so they follow the theme
        // deterministically instead of depending on scheme-derived defaults.
        CompositionLocalProvider(LocalContentColor provides CfTextPrimary) {
            content()
        }
    }
}
