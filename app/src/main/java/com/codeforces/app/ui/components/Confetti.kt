package com.codeforces.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.min
import kotlin.random.Random

private const val ConfettiDurationMillis = 2800

private class ConfettiParticle(
    val angleDeg: Float,      // burst direction
    val speed: Float,         // px per unit progress
    val color: Color,
    val width: Float,
    val height: Float,
    val spin: Float,          // degrees per unit progress
    val phase: Float,         // wobble phase offset
    val circle: Boolean
)

private val confettiPalette = listOf(
    Color(0xFF4CAF50), Color(0xFF66BB6A), Color(0xFF81C784),
    Color(0xFF00BFA5), Color(0xFF64FFDA), Color(0xFFFFD54F),
    Color(0xFFFFCA28), Color(0xFF64B5F6), Color(0xFFFFFFFF)
)

/**
 * One-shot confetti burst exploding from [originFraction] of the available
 * bounds (default: centered, slightly above middle). Pure Canvas physics —
 * no dependencies. Remains visible (transparent) after finishing so the
 * caller controls lifetime via its own state.
 */
@Composable
fun ConfettiBurst(
    modifier: Modifier = Modifier,
    originFraction: Offset = Offset(0.5f, 0.38f),
    colors: List<Color> = confettiPalette,
    particleCount: Int = 90
) {
    BoxWithConstraints(modifier.clipToBounds()) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val scale = min(widthPx, heightPx)

        var particles by remember {
            mutableStateOf<List<ConfettiParticle>>(emptyList())
        }
        if (particles.isEmpty() && widthPx > 0f) {
            val rng = Random.Default
            particles = List(particleCount) {
                val angle = rng.nextFloat() * 360f
                ConfettiParticle(
                    angleDeg = angle,
                    // Bias most particles upward: strongly damp downward speeds.
                    speed = (0.35f + rng.nextFloat() * 0.65f) * scale *
                        (if (angle in 90f..270f) 0.55f else 1f),
                    color = colors[rng.nextInt(colors.size)],
                    width = (4f + rng.nextFloat() * 5f) * (scale / 500f).coerceIn(0.6f, 1.6f),
                    height = (7f + rng.nextFloat() * 7f) * (scale / 500f).coerceIn(0.6f, 1.6f),
                    spin = (if (rng.nextBoolean()) 1f else -1f) * (240f + rng.nextFloat() * 420f),
                    phase = rng.nextFloat() * (2f * Math.PI.toFloat()),
                    circle = rng.nextInt(100) < 25
                )
            }
        }

        val progress = remember { Animatable(0f) }
        LaunchedEffect(Unit) {
            progress.animateTo(1f, tween(ConfettiDurationMillis, easing = LinearEasing))
        }
        val p = progress.value

        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            if (particles.isEmpty()) return@Canvas
            val ox = size.width * originFraction.x
            val oy = size.height * originFraction.y
            val rad = Math.PI.toFloat() / 180f
            particles.forEach { particle ->
                // Distance travelled: decelerating burst + gravity pull-down.
                val travel = particle.speed * (1f - 0.55f * p) * p
                val x = ox + kotlin.math.cos(particle.angleDeg * rad) * travel +
                    kotlin.math.sin(p * 9f + particle.phase) * 14f * p
                val y = oy + kotlin.math.sin(particle.angleDeg * rad) * travel +
                    0.5f * 1.35f * scale * p * p   // gravity
                if (y > size.height + 40f) return@forEach

                val alpha = when {
                    p < 0.55f -> 1f
                    else -> (1f - (p - 0.55f) / 0.45f).coerceIn(0f, 1f)
                }
                val rotationDeg = particle.spin * p
                drawParticle(particle, Offset(x, y), rotationDeg, alpha, abs(particle.phase))
            }
        }
    }
}

private fun DrawScope.drawParticle(
    particle: ConfettiParticle,
    position: Offset,
    rotationDeg: Float,
    alpha: Float,
    seed: Float
) {
    val color = particle.color.copy(alpha = alpha)
    // Slight thickness oscillation to fake 3D paper flipping.
    val squash = 0.35f + 0.65f * abs(kotlin.math.sin(rotationDeg * 0.017f + seed))
    val w = particle.width * squash
    val h = particle.height
    rotate(rotationDeg, pivot = position) {
        if (particle.circle) {
            drawCircle(
                color = color,
                radius = w * 0.7f,
                center = position
            )
        } else {
            drawRoundRect(
                color = color,
                topLeft = Offset(position.x - w / 2f, position.y - h / 2f),
                size = Size(w, h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.25f)
            )
        }
    }
}
