package com.onyx.avhub.core.common

import com.google.common.truth.Truth.assertThat
import com.onyx.avhub.core.common.util.AudioMath
import org.junit.Test

class AudioMathTest {

    @Test
    fun `unity gain is zero dB`() {
        assertThat(AudioMath.linearToDb(1.0)).isWithin(1e-9).of(0.0)
    }

    @Test
    fun `zero dB round trips to unity gain`() {
        assertThat(AudioMath.dbToLinear(0.0)).isWithin(1e-9).of(1.0)
    }

    @Test
    fun `plus six dB is approximately double amplitude`() {
        assertThat(AudioMath.dbToLinear(6.0)).isWithin(0.01).of(1.995)
    }

    @Test
    fun `clampDb restricts to the configured range`() {
        assertThat(AudioMath.clampDb(100.0)).isEqualTo(24.0)
        assertThat(AudioMath.clampDb(-100.0)).isEqualTo(-24.0)
        assertThat(AudioMath.clampDb(3.5)).isEqualTo(3.5)
    }
}
