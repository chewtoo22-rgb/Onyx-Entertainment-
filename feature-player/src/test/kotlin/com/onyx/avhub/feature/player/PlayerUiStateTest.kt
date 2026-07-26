package com.onyx.avhub.feature.player

import androidx.media3.common.Player
import com.google.common.truth.Truth.assertThat
import com.onyx.avhub.core.media.codec.DecoderPreference
import com.onyx.avhub.feature.player.ui.PlayerUiState
import org.junit.Test

class PlayerUiStateTest {

    @Test
    fun `default state is idle and not playing`() {
        val state = PlayerUiState()
        assertThat(state.isPlaying).isFalse()
        assertThat(state.playbackState).isEqualTo(Player.STATE_IDLE)
        assertThat(state.decoderPreferences).isEmpty()
    }

    @Test
    fun `decoder preferences track per mime type overrides`() {
        val state = PlayerUiState(decoderPreferences = mapOf("video/hevc" to DecoderPreference.FORCE_HARDWARE))
        assertThat(state.decoderPreferences["video/hevc"]).isEqualTo(DecoderPreference.FORCE_HARDWARE)
        assertThat(state.decoderPreferences["video/avc"]).isNull()
    }
}
