package com.onyx.avhub.feature.audiohub.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * Soft blurred color blobs behind the visualizer, pulsing with [bassIntensity] (0f..1f, the
 * low-frequency band from [com.onyx.avhub.core.dsp.filter.SpectrumAnalyzer]).
 *
 * Built on Compose's own cross-platform `RenderEffect`/`BlurEffect` API, which the framework
 * itself documents as a no-op below Android 12 — no manual SDK-version branching needed here,
 * older devices simply render the blobs unblurred (still visible, just crisper edges).
 *
 * This blurs the decorative shapes themselves, not the content drawn on top of them, so it's
 * an ambient glow rather than true backdrop (glassmorphism) blur — that would need capturing
 * and re-compositing the layers beneath the panel, a much heavier lift for comparatively little
 * extra impact in this layout.
 */
@Composable
fun AmbientGlow(
    bassIntensity: Float,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF00E5C7),
    secondaryColor: Color = Color(0xFF3DA9FC),
) {
    val pulse by animateFloatAsState(
        targetValue = bassIntensity.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 220),
        label = "ambientGlowPulse",
    )

    Box(modifier = modifier) {
        GlowBlob(
            color = primaryColor,
            pulse = pulse,
            baseSizeDp = 180,
            modifier = Modifier.align(Alignment.TopStart).offset((-40).dp, (-30).dp),
        )
        GlowBlob(
            color = secondaryColor,
            pulse = pulse,
            baseSizeDp = 220,
            modifier = Modifier.align(Alignment.BottomEnd).offset(40.dp, 30.dp),
        )
    }
}

@Composable
private fun GlowBlob(color: Color, pulse: Float, baseSizeDp: Int, modifier: Modifier = Modifier) {
    val sizeDp = (baseSizeDp * (0.85f + 0.3f * pulse)).dp
    Box(
        modifier = modifier
            .size(sizeDp)
            .graphicsLayer {
                renderEffect = BlurEffect(radiusX = 70f, radiusY = 70f)
                compositingStrategy = CompositingStrategy.Offscreen
                alpha = 0.35f + 0.35f * pulse
            }
            .background(
                brush = Brush.radialGradient(listOf(color, color.copy(alpha = 0f))),
            ),
    )
}
