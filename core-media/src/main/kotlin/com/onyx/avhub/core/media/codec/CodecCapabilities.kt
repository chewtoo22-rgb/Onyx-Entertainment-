package com.onyx.avhub.core.media.codec

import android.media.MediaCodecInfo as PlatformMediaCodecInfo
import android.media.MediaCodecList

data class DecoderCapability(
    val name: String,
    val mimeType: String,
    val isHardwareAccelerated: Boolean,
)

/** Video/audio mime types the enhancement hub exposes toggles for, mapped to friendly labels. */
object OnyxCodecs {
    val VIDEO = linkedMapOf(
        "video/avc" to "H.264 / AVC",
        "video/hevc" to "H.265 / HEVC",
        "video/x-vnd.on2.vp9" to "VP9",
        "video/av01" to "AV1",
    )

    val AUDIO = linkedMapOf(
        "audio/mp4a-latm" to "AAC",
        "audio/opus" to "Opus",
        "audio/flac" to "FLAC",
        "audio/mpeg" to "MP3",
        "audio/vorbis" to "Vorbis",
    )
}

/** Enumerates the platform's actual decoder list, so the UI only shows codecs this device can play. */
class CodecCapabilitiesInspector {

    fun decodersFor(mimeType: String): List<DecoderCapability> {
        val list = MediaCodecList(MediaCodecList.REGULAR_CODECS)
        return list.codecInfos
            .asSequence()
            .filter { !it.isEncoder }
            .flatMap { info -> info.supportedTypes.asSequence().map { info to it } }
            .filter { (_, type) -> type.equals(mimeType, ignoreCase = true) }
            .map { (info, type) ->
                DecoderCapability(
                    name = info.name,
                    mimeType = type,
                    isHardwareAccelerated = info.isLikelyHardwareAccelerated(),
                )
            }
            .toList()
    }

    /**
     * Name-based heuristic instead of the real [PlatformMediaCodecInfo.isHardwareAccelerated],
     * which only exists from API 29 — this module supports minSdk 26.
     */
    private fun PlatformMediaCodecInfo.isLikelyHardwareAccelerated(): Boolean =
        !name.startsWith("OMX.google.") && !name.startsWith("c2.android.")
}
