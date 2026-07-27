package com.onyx.avhub.core.videogl.pipeline

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shared mutable state for the before/after comparison shader: the UI thread writes these as
 * the user drags the divider or the strength slider, and the GL rendering thread reads them
 * once per frame in [com.onyx.avhub.core.videogl.effects.ComparisonShaderProgram.drawFrame].
 *
 * `@Volatile` gives the GL thread visibility of the latest value without needing to rebuild
 * the whole effect chain (and re-`prepare()` the player) on every drag movement.
 */
@Singleton
class VideoComparisonState @Inject constructor() {
    @Volatile var splitPosition: Float = 0.5f
        private set

    @Volatile var sharpenStrength: Float = 0.6f
        private set

    fun setSplitPosition(value: Float) {
        splitPosition = value.coerceIn(0f, 1f)
    }

    fun setSharpenStrength(value: Float) {
        sharpenStrength = value.coerceIn(0f, 1f)
    }
}
