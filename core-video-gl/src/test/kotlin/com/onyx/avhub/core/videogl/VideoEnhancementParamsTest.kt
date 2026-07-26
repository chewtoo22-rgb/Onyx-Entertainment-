package com.onyx.avhub.core.videogl

import com.google.common.truth.Truth.assertThat
import com.onyx.avhub.core.videogl.pipeline.VideoEnhancementParams
import org.junit.Test

class VideoEnhancementParamsTest {

    @Test
    fun `default params are neutral`() {
        assertThat(VideoEnhancementParams().isNeutral).isTrue()
    }

    @Test
    fun `any non-default field makes params non-neutral`() {
        assertThat(VideoEnhancementParams(sharpenStrength = 0.5f).isNeutral).isFalse()
        assertThat(VideoEnhancementParams(contrast = 0.2f).isNeutral).isFalse()
    }
}
