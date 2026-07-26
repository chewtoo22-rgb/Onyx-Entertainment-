package com.onyx.avhub.core.dsp

import com.google.common.truth.Truth.assertThat
import com.onyx.avhub.core.dsp.filter.BiquadDesigner
import org.junit.Test

/**
 * At 0 dB gain every RBJ filter shape must reduce to an exact identity transfer function
 * (numerator coefficients equal denominator coefficients), i.e. a true bypass rather than
 * merely "close to flat". That's a precise, checkable property independent of frequency/Q.
 */
class BiquadDesignerTest {

    private val sampleRate = 48000.0

    @Test
    fun `peaking eq at 0dB is an exact bypass`() {
        val c = BiquadDesigner.peakingEq(sampleRate, centerFreqHz = 1000.0, q = 0.707, gainDb = 0.0)
        assertThat(c.b0).isWithin(1e-9).of(1.0)
        assertThat(c.b1).isWithin(1e-9).of(c.a1)
        assertThat(c.b2).isWithin(1e-9).of(c.a2)
    }

    @Test
    fun `low shelf at 0dB is an exact bypass`() {
        val c = BiquadDesigner.lowShelf(sampleRate, cutoffFreqHz = 250.0, q = 0.707, gainDb = 0.0)
        assertThat(c.b0).isWithin(1e-9).of(1.0)
        assertThat(c.b1).isWithin(1e-9).of(c.a1)
        assertThat(c.b2).isWithin(1e-9).of(c.a2)
    }

    @Test
    fun `high shelf at 0dB is an exact bypass`() {
        val c = BiquadDesigner.highShelf(sampleRate, cutoffFreqHz = 8000.0, q = 0.707, gainDb = 0.0)
        assertThat(c.b0).isWithin(1e-9).of(1.0)
        assertThat(c.b1).isWithin(1e-9).of(c.a1)
        assertThat(c.b2).isWithin(1e-9).of(c.a2)
    }

    @Test
    fun `boosting gain changes coefficients away from bypass`() {
        val flat = BiquadDesigner.peakingEq(sampleRate, centerFreqHz = 1000.0, q = 1.0, gainDb = 0.0)
        val boosted = BiquadDesigner.peakingEq(sampleRate, centerFreqHz = 1000.0, q = 1.0, gainDb = 6.0)
        assertThat(boosted.b0).isNotEqualTo(flat.b0)
    }

    @Test
    fun `coefficients are finite across the full band range`() {
        val centerFreqs = listOf(31.0, 62.0, 125.0, 250.0, 500.0, 1000.0, 2000.0, 4000.0, 8000.0, 16000.0)
        for (freq in centerFreqs) {
            val c = BiquadDesigner.peakingEq(sampleRate, freq, q = 1.41, gainDb = 12.0)
            assertThat(c.b0.isFinite()).isTrue()
            assertThat(c.b1.isFinite()).isTrue()
            assertThat(c.b2.isFinite()).isTrue()
            assertThat(c.a1.isFinite()).isTrue()
            assertThat(c.a2.isFinite()).isTrue()
        }
    }
}
