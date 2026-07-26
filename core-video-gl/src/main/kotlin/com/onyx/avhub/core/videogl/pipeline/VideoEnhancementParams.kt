package com.onyx.avhub.core.videogl.pipeline

/** Upscaling algorithm choices, from cheapest to most GPU-intensive. */
enum class ScalingAlgorithm { NEAREST, BILINEAR, BICUBIC, LANCZOS3, EDGE_DIRECTED }

enum class ToneMapCurve { NONE, REINHARD, ACES }

/**
 * Full parameter set for the video enhancement pipeline. Phase 3 wires this into an
 * always-on [com.onyx.avhub.core.videogl.effects.IdentityGlEffect] to prove the chain
 * end-to-end; Phase 4 replaces the identity pass with real per-pixel shaders that read
 * these values as uniforms.
 */
data class VideoEnhancementParams(
    val scalingAlgorithm: ScalingAlgorithm = ScalingAlgorithm.BILINEAR,
    val sharpenStrength: Float = 0f, // 0..1
    val denoiseStrength: Float = 0f, // 0..1
    val contrast: Float = 0f, // -1..1
    val saturation: Float = 0f, // -1..1
    val gamma: Float = 1f, // 0.1..3
    val colorTemperatureShift: Float = 0f, // -1 (cooler) .. 1 (warmer)
    val toneMapCurve: ToneMapCurve = ToneMapCurve.NONE,
    val deinterlaceEnabled: Boolean = false,
) {
    val isNeutral: Boolean
        get() = this == VideoEnhancementParams()
}
