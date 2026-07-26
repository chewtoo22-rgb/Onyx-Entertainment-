package com.onyx.avhub.core.media.codec

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.mediacodec.MediaCodecInfo
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.mediacodec.MediaCodecUtil.DecoderQueryException

/**
 * Wraps the platform's decoder list so users can force or prefer hardware/software
 * decoding per codec (exposed in the player's codec settings screen), instead of
 * always taking whatever order the platform reports.
 */
@OptIn(markerClass = [UnstableApi::class])
class OnyxMediaCodecSelector(
    private val preferenceForMimeType: (mimeType: String) -> DecoderPreference,
) : MediaCodecSelector {

    @Throws(DecoderQueryException::class)
    override fun getDecoderInfos(
        mimeType: String,
        requiresSecureDecoder: Boolean,
        requiresTunnelingDecoder: Boolean,
    ): List<MediaCodecInfo> {
        val candidates = MediaCodecSelector.DEFAULT.getDecoderInfos(
            mimeType,
            requiresSecureDecoder,
            requiresTunnelingDecoder,
        )
        return applyPreference(candidates, preferenceForMimeType(mimeType))
    }

    companion object {
        /** Pure ordering/filtering logic, split out from [getDecoderInfos] so it's unit-testable
         *  without needing the platform's real [MediaCodecSelector.DEFAULT]. */
        fun applyPreference(
            candidates: List<MediaCodecInfo>,
            preference: DecoderPreference,
        ): List<MediaCodecInfo> = when (preference) {
            DecoderPreference.AUTO -> candidates
            DecoderPreference.PREFER_HARDWARE -> candidates.sortedByDescending { it.hardwareAccelerated }
            DecoderPreference.PREFER_SOFTWARE -> candidates.sortedByDescending { it.softwareOnly }
            DecoderPreference.FORCE_HARDWARE -> candidates.filter { it.hardwareAccelerated }
            DecoderPreference.FORCE_SOFTWARE -> candidates.filter { it.softwareOnly }
        }
    }
}
