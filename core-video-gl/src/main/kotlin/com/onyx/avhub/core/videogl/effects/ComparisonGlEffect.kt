package com.onyx.avhub.core.videogl.effects

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.GlEffect
import androidx.media3.effect.GlShaderProgram
import com.onyx.avhub.core.videogl.pipeline.VideoComparisonState

/** [GlEffect] wrapper for [ComparisonShaderProgram]. */
@OptIn(markerClass = [UnstableApi::class])
class ComparisonGlEffect(private val state: VideoComparisonState) : GlEffect {
    override fun toGlShaderProgram(context: Context, useHdr: Boolean): GlShaderProgram =
        ComparisonShaderProgram(state)
}
