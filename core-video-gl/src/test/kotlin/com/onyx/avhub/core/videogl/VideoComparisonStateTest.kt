package com.onyx.avhub.core.videogl

import com.google.common.truth.Truth.assertThat
import com.onyx.avhub.core.videogl.pipeline.VideoComparisonState
import org.junit.Test

class VideoComparisonStateTest {

    @Test
    fun `defaults to a centered split and moderate sharpen`() {
        val state = VideoComparisonState()
        assertThat(state.splitPosition).isEqualTo(0.5f)
        assertThat(state.sharpenStrength).isEqualTo(0.6f)
    }

    @Test
    fun `setSplitPosition clamps to 0 and 1`() {
        val state = VideoComparisonState()
        state.setSplitPosition(-0.5f)
        assertThat(state.splitPosition).isEqualTo(0f)
        state.setSplitPosition(1.5f)
        assertThat(state.splitPosition).isEqualTo(1f)
        state.setSplitPosition(0.3f)
        assertThat(state.splitPosition).isEqualTo(0.3f)
    }

    @Test
    fun `setSharpenStrength clamps to 0 and 1`() {
        val state = VideoComparisonState()
        state.setSharpenStrength(-1f)
        assertThat(state.sharpenStrength).isEqualTo(0f)
        state.setSharpenStrength(2f)
        assertThat(state.sharpenStrength).isEqualTo(1f)
    }
}
