package com.onyx.avhub.feature.audiohub.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch

/**
 * Glowing bar-style spectrum visualizer driven by [bands] (0f..1f per bar, log-frequency
 * spaced — see [com.onyx.avhub.core.dsp.filter.SpectrumAnalyzer]). Each bar animates with a
 * fast attack / slow decay, the classic VU-meter feel, so it reads as "reacting to the beat"
 * rather than jittering with the raw FFT noise.
 */
@Composable
fun SpectrumVisualizerView(
    bands: FloatArray,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
) {
    val animatables = remember(bands.size) { List(bands.size) { Animatable(0f) } }

    LaunchedEffect(bands) {
        bands.forEachIndexed { index, target ->
            val animatable = animatables[index]
            launch {
                val isRising = target > animatable.value
                animatable.animateTo(
                    targetValue = target,
                    animationSpec = if (isRising) {
                        tween(durationMillis = 60, easing = LinearOutSlowInEasing)
                    } else {
                        tween(durationMillis = 400, easing = FastOutSlowInEasing)
                    },
                )
            }
        }
    }

    Canvas(modifier = modifier) {
        if (animatables.isEmpty()) return@Canvas
        val barCount = animatables.size
        val gap = size.width * 0.015f
        val barWidth = (size.width - gap * (barCount - 1)) / barCount

        animatables.forEachIndexed { index, animatable ->
            val heightFraction = animatable.value.coerceIn(0f, 1f)
            val barHeight = (size.height * heightFraction).coerceAtLeast(barWidth)
            val left = index * (barWidth + gap)
            drawRoundRect(
                color = barColor,
                topLeft = Offset(left, size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
                alpha = 0.45f + 0.55f * heightFraction,
            )
        }
    }
}
