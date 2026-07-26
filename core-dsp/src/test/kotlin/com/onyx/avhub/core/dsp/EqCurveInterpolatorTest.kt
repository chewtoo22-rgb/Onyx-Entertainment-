package com.onyx.avhub.core.dsp

import com.google.common.truth.Truth.assertThat
import com.onyx.avhub.core.dsp.filter.EqCurveInterpolator
import org.junit.Test

class EqCurveInterpolatorTest {

    private val centerFreqs = listOf(100, 1000, 10000)
    private val gains = listOf(0f, 6f, 0f)

    @Test
    fun `exact center frequency returns its own gain`() {
        assertThat(EqCurveInterpolator.gainAtHz(1000.0, centerFreqs, gains)).isWithin(1e-9).of(6.0)
    }

    @Test
    fun `below lowest band clamps to first gain`() {
        assertThat(EqCurveInterpolator.gainAtHz(20.0, centerFreqs, gains)).isWithin(1e-9).of(0.0)
    }

    @Test
    fun `above highest band clamps to last gain`() {
        assertThat(EqCurveInterpolator.gainAtHz(20000.0, centerFreqs, gains)).isWithin(1e-9).of(0.0)
    }

    @Test
    fun `midpoint in log-frequency space is halfway between neighboring gains`() {
        // geometric mean of 100 and 1000 is ~316.2 Hz, the log-space midpoint.
        val midFreq = Math.sqrt(100.0 * 1000.0)
        assertThat(EqCurveInterpolator.gainAtHz(midFreq, centerFreqs, gains)).isWithin(1e-6).of(3.0)
    }
}
