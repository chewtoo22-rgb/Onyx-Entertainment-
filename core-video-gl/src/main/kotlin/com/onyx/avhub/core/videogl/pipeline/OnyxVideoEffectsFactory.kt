package com.onyx.avhub.core.videogl.pipeline

import androidx.annotation.OptIn
import androidx.media3.common.Effect
import androidx.media3.common.util.UnstableApi
import com.onyx.avhub.core.videogl.effects.IdentityGlEffect
import javax.inject.Inject

/**
 * Builds the ordered [Effect] chain handed to Media3's video effects pipeline
 * (`ExoPlayer.Builder.setVideoEffects` / `Transformer`).
 *
 * Only the identity pass exists today (Phase 3 scaffold); each Phase 4 filter
 * (scaling, sharpen, denoise, color grade, tone-map, deinterlace) becomes one more
 * [Effect] appended here once its shader is implemented, gated on [params] so
 * neutral settings don't cost a GL pass.
 */
class OnyxVideoEffectsFactory @Inject constructor() {

    @OptIn(markerClass = [UnstableApi::class])
    fun buildEffects(params: VideoEnhancementParams): List<Effect> {
        val effects = mutableListOf<Effect>()
        effects += IdentityGlEffect()
        return effects
    }
}
