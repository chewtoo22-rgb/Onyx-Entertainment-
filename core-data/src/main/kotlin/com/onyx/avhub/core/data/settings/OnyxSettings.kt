package com.onyx.avhub.core.data.settings

/** Video quality/performance tradeoff for the GL enhancement pipeline. */
enum class VideoQualityMode { BATTERY_SAVER, BALANCED, MAX_QUALITY }

data class OnyxSettings(
    val systemAudioHubEnabled: Boolean = false,
    val activePresetId: Long = 1L,
    val perAppProfilesEnabled: Boolean = false,
    val loudnessNormalizationEnabled: Boolean = false,
    val videoQualityMode: VideoQualityMode = VideoQualityMode.BALANCED,
    val preferHardwareDecoding: Boolean = true,
    val hdrToneMappingEnabled: Boolean = true,
    /** Enables Media3's floating-point audio output path for bit-perfect/Hi-Res playback. */
    val hiResAudioEnabled: Boolean = false,
)
