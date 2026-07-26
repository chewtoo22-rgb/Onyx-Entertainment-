package com.onyx.avhub.core.dsp.filter

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** Second-order IIR filter coefficients, normalized so a0 == 1. */
data class BiquadCoefficients(
    val b0: Double,
    val b1: Double,
    val b2: Double,
    val a0: Double,
    val a1: Double,
    val a2: Double,
) {
    fun normalized(): BiquadCoefficients =
        if (a0 == 1.0) this else BiquadCoefficients(b0 / a0, b1 / a0, b2 / a0, 1.0, a1 / a0, a2 / a0)
}

/**
 * Filter design formulas from Robert Bristow-Johnson's "Audio EQ Cookbook", used to turn
 * a band's (frequency, Q, gain) into biquad coefficients for both the parametric equalizer
 * UI preview and the native [com.onyx.avhub.core.dsp.AudioDspEngine] processing chain.
 */
object BiquadDesigner {

    fun peakingEq(sampleRateHz: Double, centerFreqHz: Double, q: Double, gainDb: Double): BiquadCoefficients {
        val a = 10.0.pow(gainDb / 40.0)
        val w0 = angularFrequency(centerFreqHz, sampleRateHz)
        val cosW0 = cos(w0)
        val alpha = sin(w0) / (2.0 * q)

        return BiquadCoefficients(
            b0 = 1 + alpha * a,
            b1 = -2 * cosW0,
            b2 = 1 - alpha * a,
            a0 = 1 + alpha / a,
            a1 = -2 * cosW0,
            a2 = 1 - alpha / a,
        ).normalized()
    }

    fun lowShelf(sampleRateHz: Double, cutoffFreqHz: Double, q: Double, gainDb: Double): BiquadCoefficients {
        val a = 10.0.pow(gainDb / 40.0)
        val w0 = angularFrequency(cutoffFreqHz, sampleRateHz)
        val cosW0 = cos(w0)
        val alpha = sin(w0) / (2.0 * q)
        val twoSqrtAAlpha = 2 * sqrt(a) * alpha

        return BiquadCoefficients(
            b0 = a * ((a + 1) - (a - 1) * cosW0 + twoSqrtAAlpha),
            b1 = 2 * a * ((a - 1) - (a + 1) * cosW0),
            b2 = a * ((a + 1) - (a - 1) * cosW0 - twoSqrtAAlpha),
            a0 = (a + 1) + (a - 1) * cosW0 + twoSqrtAAlpha,
            a1 = -2 * ((a - 1) + (a + 1) * cosW0),
            a2 = (a + 1) + (a - 1) * cosW0 - twoSqrtAAlpha,
        ).normalized()
    }

    fun highShelf(sampleRateHz: Double, cutoffFreqHz: Double, q: Double, gainDb: Double): BiquadCoefficients {
        val a = 10.0.pow(gainDb / 40.0)
        val w0 = angularFrequency(cutoffFreqHz, sampleRateHz)
        val cosW0 = cos(w0)
        val alpha = sin(w0) / (2.0 * q)
        val twoSqrtAAlpha = 2 * sqrt(a) * alpha

        return BiquadCoefficients(
            b0 = a * ((a + 1) + (a - 1) * cosW0 + twoSqrtAAlpha),
            b1 = -2 * a * ((a - 1) + (a + 1) * cosW0),
            b2 = a * ((a + 1) + (a - 1) * cosW0 - twoSqrtAAlpha),
            a0 = (a + 1) - (a - 1) * cosW0 + twoSqrtAAlpha,
            a1 = 2 * ((a - 1) - (a + 1) * cosW0),
            a2 = (a + 1) - (a - 1) * cosW0 - twoSqrtAAlpha,
        ).normalized()
    }

    private fun angularFrequency(freqHz: Double, sampleRateHz: Double): Double =
        2.0 * PI * freqHz / sampleRateHz
}
