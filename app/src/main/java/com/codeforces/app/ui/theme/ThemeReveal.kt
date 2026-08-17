package com.codeforces.app.ui.theme

import android.app.Activity
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import kotlin.math.sqrt

/**
 * Circular-reveal theme transition: the screen is captured in the OLD theme
 * just before toggling, the theme flips instantly underneath, and this
 * screenshot is laid on top with an expanding transparent circle punched out
 * from the toggle icon — the new theme sweeps across the page in a curve.
 */
object ThemeRevealState {
    var snapshot: ImageBitmap? by mutableStateOf(null)
    var center: Offset by mutableStateOf(Offset.Zero)
}

/**
 * Async window capture via PixelCopy. The view tree contains hardware
 * bitmaps (Coil images), which throw "Software rendering doesn't support
 * hardware bitmaps" when drawn onto a software canvas — so View.draw(Canvas)
 * is unusable here. PixelCopy reads the already-rendered surface instead.
 * [onCaptured] receives null on failure (reveal is skipped, theme still flips).
 */
fun captureWindowBitmap(view: View, onCaptured: (ImageBitmap?) -> Unit) {
    val window = (view.context as? Activity)?.window
    if (window == null) {
        onCaptured(null)
        return
    }
    val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
    try {
        PixelCopy.request(window, bitmap, { result ->
            onCaptured(if (result == PixelCopy.SUCCESS) bitmap.asImageBitmap() else null)
        }, Handler(Looper.getMainLooper()))
    } catch (_: Exception) {
        onCaptured(null)
    }
}

/**
 * Mount as the last child of the root Box, OUTSIDE any key(...) block that
 * rebuilds on theme flips — otherwise the disposal would cancel the reveal
 * animation mid-flight.
 */
@Composable
fun BoxScope.ThemeRevealOverlay() {
    val snapshot = ThemeRevealState.snapshot
    if (snapshot != null) {
        val center = ThemeRevealState.center
        var progress by remember(snapshot) { mutableFloatStateOf(0f) }

        LaunchedEffect(snapshot) {
            animate(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
            ) { value, _ -> progress = value }
            ThemeRevealState.snapshot = null
        }

        Image(
            bitmap = snapshot,
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    val farX = maxOf(center.x, size.width - center.x)
                    val farY = maxOf(center.y, size.height - center.y)
                    val maxRadius = sqrt(farX * farX + farY * farY)
                    drawCircle(
                        color = Color.Black,
                        radius = progress * maxRadius,
                        center = center,
                        blendMode = BlendMode.Clear
                    )
                }
        )
    }
}
