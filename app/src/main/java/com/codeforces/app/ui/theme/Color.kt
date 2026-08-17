package com.codeforces.app.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * Global theme switch, backed by snapshot state so every color below
 * recomposes live when toggled. Persisted by UserPreferencesRepository
 * ("dark_theme", default true) and applied at app start by MainActivity.
 *
 * [darkProgress] is the animated blend factor (0f = light, 1f = dark) that
 * CodeforcesTheme tweens toward [isDark] on every toggle — colors lerp
 * smoothly instead of snapping.
 */
object CfThemeState {
    var isDark: Boolean by mutableStateOf(true)
    var darkProgress: Float by mutableFloatStateOf(1f)
}

/** Blend a light/dark color pair by the current animation progress. */
private fun cfColor(light: Long, dark: Long): Color =
    lerp(Color(light), Color(dark), CfThemeState.darkProgress)

// ── Brand accent (teal) ──────────────────────────────────────────────────────
// Light mode uses a deeper teal so accent-on-white keeps ≥4.5:1 contrast.

val CodeforcesAccent: Color get() = cfColor(0xFF00796B, 0xFF00BFA5)
val CfAccentLight: Color get() = cfColor(0xFF00695C, 0xFF64FFDA)
val CfAccentDark: Color get() = cfColor(0xFF00BFA5, 0xFF00897B)

// ── Surfaces ─────────────────────────────────────────────────────────────────
// Light: near-white gray background, pure white surfaces, and a faint
// teal-tinted card so cards read as a layer above the surface.

val CfBackground: Color get() = cfColor(0xFFFAFAFA, 0xFF121212)
val CfSurface: Color get() = cfColor(0xFFFFFFFF, 0xFF1E1E1E)
val CfCardSurface: Color get() = cfColor(0xFFF1F4F3, 0xFF252525)
val CfDivider: Color get() = cfColor(0xFFE4E7E6, 0xFF303030)

// ── Text ─────────────────────────────────────────────────────────────────────

val CfTextPrimary: Color get() = cfColor(0xFF1B1F1E, 0xFFEEEEEE)
val CfTextSecondary: Color get() = cfColor(0xFF5B6866, 0xFF9E9E9E)
val CfTextDisabled: Color get() = cfColor(0xFF9AA5A3, 0xFF616161)

// ── Rating colors (Codeforces standard, adjusted per theme for contrast) ─────

val RatingNewbie: Color get() = cfColor(0xFF607D8B, 0xFFB0BEC5)
val RatingPupil: Color get() = cfColor(0xFF2E7D32, 0xFF66BB6A)
val RatingApprentice: Color get() = cfColor(0xFF00838F, 0xFF26C6DA)
val RatingSpecialist: Color get() = cfColor(0xFF00838F, 0xFF26C6DA)
val RatingExpert: Color get() = cfColor(0xFF1565C0, 0xFF64B5F6)
val RatingCM: Color get() = cfColor(0xFF8E24AA, 0xFFBA68C8)
val RatingIM: Color get() = cfColor(0xFFEF6C00, 0xFFFFA726)
val RatingGM: Color get() = cfColor(0xFFC62828, 0xFFEF5350)
val RatingIGM: Color get() = cfColor(0xFFC62828, 0xFFEF5350)
val RatingLGM: Color get() = cfColor(0xFFC62828, 0xFFEF5350)

// ── Verdict colors ───────────────────────────────────────────────────────────
// Light variants use the 700/800 tones — the dark-theme 300/400 tones
// (especially the yellow COMPILATION_ERROR) are illegible on white.

val VerdictOK: Color get() = cfColor(0xFF2E7D32, 0xFF4CAF50)
val VerdictWA: Color get() = cfColor(0xFFD32F2F, 0xFFF44336)
val VerdictTLE: Color get() = cfColor(0xFFE65100, 0xFFFF9800)
val VerdictMLE: Color get() = cfColor(0xFF7B1FA2, 0xFF9C27B0)
val VerdictRTE: Color get() = cfColor(0xFF1565C0, 0xFF2196F3)
val VerdictCE: Color get() = cfColor(0xFFA17800, 0xFFFFEB3B)
val VerdictSkipped: Color get() = cfColor(0xFF616161, 0xFF9E9E9E)
