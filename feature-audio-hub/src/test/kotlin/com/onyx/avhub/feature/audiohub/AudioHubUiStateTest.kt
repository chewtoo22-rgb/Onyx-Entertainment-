package com.onyx.avhub.feature.audiohub

import com.google.common.truth.Truth.assertThat
import com.onyx.avhub.core.data.db.entity.EqPresetEntity
import com.onyx.avhub.feature.audiohub.ui.AudioHubUiState
import org.junit.Test

class AudioHubUiStateTest {

    private fun preset(id: Long, name: String) =
        EqPresetEntity(id = id, name = name, bandCenterFreqHz = listOf(1000), bandGainsDb = listOf(0f))

    @Test
    fun `activePreset resolves the preset matching activePresetId`() {
        val state = AudioHubUiState(
            presets = listOf(preset(1, "Flat"), preset(2, "Bass Boost")),
            activePresetId = 2,
        )
        assertThat(state.activePreset?.name).isEqualTo("Bass Boost")
    }

    @Test
    fun `activePreset falls back to the first preset when id is unmatched`() {
        val state = AudioHubUiState(
            presets = listOf(preset(1, "Flat"), preset(2, "Bass Boost")),
            activePresetId = 999,
        )
        assertThat(state.activePreset?.name).isEqualTo("Flat")
    }

    @Test
    fun `activePreset is null when there are no presets yet`() {
        assertThat(AudioHubUiState().activePreset).isNull()
    }
}
