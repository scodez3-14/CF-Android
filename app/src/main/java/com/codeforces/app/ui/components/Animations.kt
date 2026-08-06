package com.codeforces.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.codeforces.app.ui.theme.CfCardSurface
import com.codeforces.app.ui.theme.CfTextPrimary
import com.codeforces.app.ui.theme.CfTextSecondary
import com.codeforces.app.ui.theme.CodeforcesAccent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val ShimmerDurationMillis = 1300

@Composable
fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(ShimmerDurationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerShift"
    )
    return Brush.linearGradient(
        colors = listOf(
            CfCardSurface,
            CfTextPrimary.copy(alpha = 0.06f),
            CfCardSurface
        ),
        start = Offset(shift - 600f, 0f),
        end = Offset(shift, 400f)
    )
}

@Composable
fun SkeletonBox(brush: Brush, modifier: Modifier = Modifier, cornerRadius: Dp = 8.dp) {
    Box(modifier.clip(RoundedCornerShape(cornerRadius)).background(brush))
}

@Composable
fun ShimmerCardRow(brush: Brush, modifier: Modifier = Modifier, shapeRadius: Dp = 14.dp) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CfCardSurface),
        shape = RoundedCornerShape(shapeRadius)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkeletonBox(brush, Modifier.size(40.dp), cornerRadius = 12.dp)
            Column(
                modifier = Modifier.weight(1f).padding(start = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SkeletonBox(brush, Modifier.fillMaxWidth(0.55f).height(14.dp), cornerRadius = 6.dp)
                SkeletonBox(brush, Modifier.fillMaxWidth(0.9f).height(12.dp), cornerRadius = 6.dp)
                SkeletonBox(brush, Modifier.fillMaxWidth(0.4f).height(10.dp), cornerRadius = 5.dp)
            }
        }
    }
}

@Composable
fun ShimmerList(
    modifier: Modifier = Modifier,
    itemCount: Int = 8,
    contentPadding: PaddingValues = PaddingValues(vertical = 8.dp)
) {
    val brush = rememberShimmerBrush()
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(itemCount) { ShimmerCardRow(brush) }
    }
}

@Composable
fun ShimmerHeroCard(brush: Brush, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CfCardSurface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkeletonBox(brush, Modifier.size(60.dp), cornerRadius = 30.dp)
            Column(
                modifier = Modifier.weight(1f).padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SkeletonBox(brush, Modifier.fillMaxWidth(0.5f).height(18.dp), cornerRadius = 6.dp)
                SkeletonBox(brush, Modifier.fillMaxWidth(0.35f).height(12.dp), cornerRadius = 5.dp)
            }
            SkeletonBox(brush, Modifier.size(54.dp, 24.dp), cornerRadius = 6.dp)
        }
    }
}

@Composable
fun RevealItem(
    visible: Boolean,
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    content: @Composable () -> Unit
) {
    var hasRevealed by rememberSaveable { mutableStateOf(false) }

    // Fast path: already revealed — skip creating Animatables entirely
    if (hasRevealed) {
        Box(modifier = modifier) { content() }
        return
    }

    val alpha = remember { Animatable(0f) }
    val slide = remember { Animatable(1f) }
    LaunchedEffect(visible) {
        if (visible && !hasRevealed) {
            if (delayMillis > 0) delay(delayMillis.toLong())
            launch { alpha.animateTo(1f, tween(300, easing = FastOutSlowInEasing)) }
            launch { slide.animateTo(0f, tween(300, easing = FastOutSlowInEasing)) }
            hasRevealed = true
        }
    }
    Box(
        modifier = modifier.graphicsLayer {
            this.alpha = alpha.value
            translationY = slide.value * 28f
        }
    ) {
        content()
    }
}

@Composable
fun AnimatedCountUpText(
    target: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = Color.Unspecified
) {
    var hasCountedUp by rememberSaveable(target) { mutableStateOf(false) }
    val animatedValue = remember { Animatable(if (hasCountedUp) target.toFloat() else 0f) }
    LaunchedEffect(target) {
        if (!hasCountedUp) {
            animatedValue.animateTo(target.toFloat(), tween(650, easing = FastOutSlowInEasing))
            hasCountedUp = true
        }
    }
    Text(
        text = animatedValue.value.roundToInt().toString(),
        modifier = modifier,
        style = style,
        color = color
    )
}

@Composable
fun RefreshIcon(isRefreshing: Boolean, modifier: Modifier = Modifier) {
    if (isRefreshing) {
        val transition = rememberInfiniteTransition(label = "refreshSpin")
        val rotation by transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing), RepeatMode.Restart),
            label = "refreshRotation"
        )
        Icon(
            imageVector = Icons.Rounded.Refresh,
            contentDescription = "Refreshing",
            tint = CodeforcesAccent,
            modifier = modifier.rotate(rotation)
        )
    } else {
        Icon(
            imageVector = Icons.Rounded.Refresh,
            contentDescription = "Refresh",
            tint = CfTextSecondary,
            modifier = modifier
        )
    }
}
