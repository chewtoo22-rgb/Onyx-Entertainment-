package com.onyx.avhub.core.videogl.pipeline

import androidx.annotation.OptIn
import androidx.media3.common.Effect
import androidx.media3.common.util.UnstableApi
import com.onyx.avhub.core.videogl.effects.ComparisonGlEffect
import javax.inject.Inject

/**
 * Builds the ordered [Effect] chain handed to Media3's video effects pipeline
 * (`ExoPlayer.setVideoEffects`).
 *
 * Today this is the before/after comparison pass (real-time unsharp-mask sharpen on one side
 * of a live-draggable split, see [ComparisonGlEffect]/[VideoComparisonState]). Each further
 * Phase 4 filter (scaling, denoise, color grade, tone-map, deinterlace) becomes one more
 * [Effect] appended here once its shader is implemented, gated on [params] so neutral settings
 * don't cost a GL pass.
 */
class OnyxVideoEffectsFactory @Inject constructor(
    private val comparisonState: VideoComparisonState,
) {

    @OptIn(markerClass = [UnstableApi::class])
    fun buildEffects(params: VideoEnhancementParams): List<Effect> {
        val effects = mutableListOf<Effect>()
        effects += ComparisonGlEffect(comparisonState)
        return effects
    }
}
