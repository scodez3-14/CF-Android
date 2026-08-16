package com.codeforces.app.ui.components

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val RevealGreen = Color(0xFF4CAF50)
private val RevealGreenSoft = Color(0xFF66BB6A)
private val RevealMint = Color(0xFFA5D6A7)

/** How long the celebration stays before it slowly fades away on its own. */
private const val AUTO_DISMISS_MS = 4000L
private const val FADE_OUT_MS = 800

/**
 * Minimal celebration overlay for an Accepted submission.
 * Dark scrim, subtle glow, clean typography, one action row.
 * Disappears slowly — fades out on tap, back, or on its own.
 */
@Composable
fun AcceptedReveal(
    problemLabel: String,
    contestId: String,
    submissionId: Long?,
    passedTests: Int,
    timeMillis: Int,
    memoryBytes: Long,
    language: String,
    onSubmitAgain: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val scrim = remember { Animatable(0f) }
    val contentY = remember { Animatable(120f) }
    val contentAlpha = remember { Animatable(0f) }
    val badgeScale = remember { Animatable(0f) }
    var closing by remember { mutableStateOf(false) }

    fun close() {
        if (closing) return
        closing = true
        scope.launch {
            launch { contentAlpha.animateTo(0f, tween(FADE_OUT_MS, easing = FastOutSlowInEasing)) }
            scrim.animateTo(0f, tween(FADE_OUT_MS, easing = FastOutSlowInEasing))
            onDismiss()
        }
    }

    LaunchedEffect(Unit) {
        launch { scrim.animateTo(1f, tween(300, easing = FastOutSlowInEasing)) }
        launch {
            contentAlpha.animateTo(1f, tween(260))
            contentY.animateTo(0f, spring(dampingRatio = 0.82f, stiffness = 320f))
        }
        launch {
            delay(180)
            badgeScale.animateTo(1f, spring(dampingRatio = 0.7f, stiffness = 280f))
        }
        // Slowly drift away on its own.
        delay(AUTO_DISMISS_MS)
        close()
    }

    BackHandler(onBack = { close() })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = scrim.value }
            .background(Color.Black.copy(alpha = 0.72f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { close() }
            )
    ) {
        ConfettiBurst(
            modifier = Modifier.fillMaxSize(),
            originFraction = androidx.compose.ui.geometry.Offset(0.5f, 0.32f)
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    translationY = contentY.value
                    alpha = contentAlpha.value
                }
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Badge ──
            Box(contentAlignment = Alignment.Center) {
                // Subtle radial glow
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        RevealGreenSoft.copy(alpha = 0.25f),
                                        Color.Transparent
                                    ),
                                    radius = size.minDimension / 2f
                                ),
                                radius = size.minDimension / 2f
                            )
                        }
                )
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(RevealGreenSoft, RevealGreen)
                            )
                        )
                        .graphicsLayer {
                            scaleX = badgeScale.value
                            scaleY = badgeScale.value
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Title ──
            Text(
                "Accepted",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = Color.White
            )

            Spacer(Modifier.height(6.dp))

            // ── Problem label ──
            Text(
                problemLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.45f),
                letterSpacing = 1.2.sp
            )

            Spacer(Modifier.height(24.dp))

            // ── Stats — compact, horizontal ──
            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RevealStat(value = "$passedTests", label = "TESTS", tint = RevealMint)
                RevealStat(value = "${timeMillis}ms", label = "TIME", tint = Color(0xFF64B5F6))
                RevealStat(value = formatMemory(memoryBytes), label = "MEM", tint = Color(0xFFCE93D8))
            }

            Spacer(Modifier.height(28.dp))

            // ── Actions ──
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(
                    onClick = {
                        onSubmitAgain()
                        close()
                    }
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color.White.copy(alpha = 0.6f))
                    Spacer(Modifier.width(4.dp))
                    Text("Again", fontSize = 13.sp, color = Color.White.copy(alpha = 0.6f))
                }
                TextButton(
                    onClick = {
                        val idPart = submissionId?.let { "/submission/$it" } ?: ""
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://codeforces.com/contest/$contestId$idPart"))
                        )
                        close()
                    }
                ) {
                    Icon(Icons.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color.White.copy(alpha = 0.6f))
                    Spacer(Modifier.width(4.dp))
                    Text("Open", fontSize = 13.sp, color = Color.White.copy(alpha = 0.6f))
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "tap to dismiss",
                color = Color.White.copy(alpha = 0.2f),
                fontSize = 10.sp,
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Composable
private fun RevealStat(value: String, label: String, tint: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = tint,
            letterSpacing = 0.3.sp
        )
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.3f),
            letterSpacing = 1.5.sp
        )
    }
}

private fun formatMemory(bytes: Long): String = when {
    bytes >= 1024 * 1024 -> "%.1fMB".format(bytes / 1048576.0)
    bytes > 0 -> "${bytes / 1024}KB"
    else -> "—"
}
