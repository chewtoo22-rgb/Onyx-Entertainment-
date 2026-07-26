package com.onyx.avhub.core.media

import androidx.media3.exoplayer.mediacodec.MediaCodecInfo
import com.google.common.truth.Truth.assertThat
import com.onyx.avhub.core.media.codec.DecoderPreference
import com.onyx.avhub.core.media.codec.OnyxMediaCodecSelector
import org.junit.Test

class OnyxMediaCodecSelectorTest {

    private fun fakeDecoder(name: String, hardwareAccelerated: Boolean, softwareOnly: Boolean) =
        MediaCodecInfo.newInstance(
            name,
            "video/avc",
            "video/avc",
            /* capabilities= */ null,
            hardwareAccelerated,
            softwareOnly,
            /* vendor= */ false,
            /* forceDisableAdaptive= */ false,
            /* forceSecure= */ false,
        )

    private val hardwareDecoder = fakeDecoder("hw.decoder", hardwareAccelerated = true, softwareOnly = false)
    private val softwareDecoder = fakeDecoder("sw.decoder", hardwareAccelerated = false, softwareOnly = true)
    private val candidates = listOf(softwareDecoder, hardwareDecoder)

    @Test
    fun `AUTO preserves platform order`() {
        val result = OnyxMediaCodecSelector.applyPreference(candidates, DecoderPreference.AUTO)
        assertThat(result).containsExactly(softwareDecoder, hardwareDecoder).inOrder()
    }

    @Test
    fun `PREFER_HARDWARE moves hardware decoders first`() {
        val result = OnyxMediaCodecSelector.applyPreference(candidates, DecoderPreference.PREFER_HARDWARE)
        assertThat(result).containsExactly(hardwareDecoder, softwareDecoder).inOrder()
    }

    @Test
    fun `FORCE_HARDWARE drops software-only decoders`() {
        val result = OnyxMediaCodecSelector.applyPreference(candidates, DecoderPreference.FORCE_HARDWARE)
        assertThat(result).containsExactly(hardwareDecoder)
    }

    @Test
    fun `FORCE_SOFTWARE drops hardware decoders`() {
        val result = OnyxMediaCodecSelector.applyPreference(candidates, DecoderPreference.FORCE_SOFTWARE)
        assertThat(result).containsExactly(softwareDecoder)
    }
}
