package com.onyx.avhub.core.dsp.filter

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * Turns the raw 8-bit FFT byte array produced by [android.media.audiofx.Visualizer.getFft]
 * into normalized per-band magnitudes for the audio-reactive spectrum visualizer.
 *
 * Pure math, no Android dependency, so it's unit-testable without an emulator/device.
 */
object SpectrumAnalyzer {

    /** sqrt(127^2 + 127^2), the largest magnitude two signed bytes can produce. */
    private const val MAX_BYTE_PAIR_MAGNITUDE = 179.6f

    /**
     * Decodes the platform's packed real-FFT byte layout: `fft[0]` is the DC component's real
     * part, and for `k` in `1 until fft.size / 2`, `fft[2k]`/`fft[2k+1]` are the real/imaginary
     * parts of bin `k`. (The single Nyquist byte at `fft[1]` is not attributed to any bin here —
     * it's one bin out of hundreds and irrelevant for a visual effect.)
     *
     * @return one magnitude per bin, length `fft.size / 2`.
     */
    fun magnitudesFromFft(fft: ByteArray): FloatArray {
        val n = fft.size
        if (n < 4) return FloatArray(0)
        val bandCount = n / 2
        val magnitudes = FloatArray(bandCount)
        magnitudes[0] = abs(fft[0].toInt()).toFloat()
        for (k in 1 until bandCount) {
            val re = fft[2 * k].toFloat()
            val im = if (2 * k + 1 < n) fft[2 * k + 1].toFloat() else 0f
            magnitudes[k] = sqrt(re * re + im * im)
        }
        return magnitudes
    }

    /**
     * Groups fine FFT bins into [bandCount] log-frequency-spaced visual bars (matching how
     * [EqCurveInterpolator] treats frequency perceptually), normalized to `0f..1f`.
     */
    fun bucketToBands(
        magnitudes: FloatArray,
        sampleRateHz: Int,
        bandCount: Int,
        minFreqHz: Double = 20.0,
        maxFreqHz: Double = 20_000.0,
    ): FloatArray {
        if (bandCount <= 0) return FloatArray(0)
        if (magnitudes.isEmpty() || sampleRateHz <= 0) return FloatArray(bandCount)

        val nyquist = sampleRateHz / 2.0
        val clampedMaxFreq = maxFreqHz.coerceAtMost(nyquist)
        val logMin = ln(minFreqHz)
        val logMax = ln(clampedMaxFreq)
        val bands = FloatArray(bandCount)

        for (band in 0 until bandCount) {
            val loFreq = exp(logMin + (logMax - logMin) * band / bandCount)
            val hiFreq = exp(logMin + (logMax - logMin) * (band + 1) / bandCount)
            val loBin = binIndexForFreq(loFreq, nyquist, magnitudes.size)
            val hiBin = binIndexForFreq(hiFreq, nyquist, magnitudes.size).coerceAtLeast(loBin + 1)

            var sum = 0f
            for (bin in loBin until hiBin.coerceAtMost(magnitudes.size)) sum += magnitudes[bin]
            val avg = sum / (hiBin - loBin)
            bands[band] = (avg / MAX_BYTE_PAIR_MAGNITUDE).coerceIn(0f, 1f)
        }
        return bands
    }

    private fun binIndexForFreq(freqHz: Double, nyquistHz: Double, binCount: Int): Int =
        (freqHz / nyquistHz * binCount).toInt().coerceIn(0, binCount - 1)
}
