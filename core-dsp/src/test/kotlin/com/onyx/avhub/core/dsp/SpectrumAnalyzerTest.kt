package com.onyx.avhub.core.dsp

import com.google.common.truth.Truth.assertThat
import com.onyx.avhub.core.dsp.filter.SpectrumAnalyzer
import org.junit.Test

class SpectrumAnalyzerTest {

    @Test
    fun `magnitudesFromFft decodes a classic 3-4-5 triangle`() {
        // n=4: bandCount=2. magnitudes[0]=|fft[0]|; bin 1 uses fft[2]=3, fft[3]=4 -> sqrt(3^2+4^2)=5.
        val fft = byteArrayOf(0, 0, 3, 4)
        val magnitudes = SpectrumAnalyzer.magnitudesFromFft(fft)
        assertThat(magnitudes).hasLength(2)
        assertThat(magnitudes[0]).isEqualTo(0f)
        assertThat(magnitudes[1]).isWithin(1e-4f).of(5f)
    }

    @Test
    fun `magnitudesFromFft handles a nonzero DC component`() {
        val fft = byteArrayOf(10, 0, 0, 0)
        val magnitudes = SpectrumAnalyzer.magnitudesFromFft(fft)
        assertThat(magnitudes[0]).isEqualTo(10f)
    }

    @Test
    fun `magnitudesFromFft returns empty for too-short input`() {
        assertThat(SpectrumAnalyzer.magnitudesFromFft(byteArrayOf(1, 2))).isEmpty()
    }

    @Test
    fun `bucketToBands produces the requested number of bands`() {
        val magnitudes = FloatArray(256) { 50f }
        val bands = SpectrumAnalyzer.bucketToBands(magnitudes, sampleRateHz = 44_100, bandCount = 32)
        assertThat(bands).hasLength(32)
    }

    @Test
    fun `bucketToBands normalizes output into the 0-1 range`() {
        val magnitudes = FloatArray(256) { 255f } // far above the max possible byte-pair magnitude
        val bands = SpectrumAnalyzer.bucketToBands(magnitudes, sampleRateHz = 44_100, bandCount = 16)
        bands.forEach { assertThat(it).isAtMost(1f) }
    }

    @Test
    fun `bucketToBands is silent for a silent input`() {
        val magnitudes = FloatArray(256) { 0f }
        val bands = SpectrumAnalyzer.bucketToBands(magnitudes, sampleRateHz = 44_100, bandCount = 16)
        bands.forEach { assertThat(it).isEqualTo(0f) }
    }

    @Test
    fun `bucketToBands handles zero bandCount gracefully`() {
        assertThat(SpectrumAnalyzer.bucketToBands(FloatArray(256), sampleRateHz = 44_100, bandCount = 0)).isEmpty()
    }
}
