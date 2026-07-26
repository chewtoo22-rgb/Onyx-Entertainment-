package com.onyx.avhub.core.dsp.filter

import kotlin.math.ln

/**
 * Interpolates a gain curve (defined at a fixed set of center frequencies, e.g. our 10-band
 * preset layout) at an arbitrary frequency. Needed because the platform [android.media.audiofx.Equalizer]
 * on a given device usually exposes a different band count/layout than the one our presets are
 * stored in, so we resample our curve at whatever center frequencies the device reports.
 *
 * Interpolation is linear in log-frequency space, which matches how the ear perceives pitch and
 * how EQ curves are conventionally drawn.
 */
object EqCurveInterpolator {

    fun gainAtHz(freqHz: Double, centerFreqsHz: List<Int>, gainsDb: List<Float>): Double {
        require(centerFreqsHz.size == gainsDb.size) { "Center frequencies and gains must pair up" }
        require(centerFreqsHz.isNotEmpty()) { "Curve must have at least one point" }

        if (centerFreqsHz.size == 1 || freqHz <= centerFreqsHz.first()) return gainsDb.first().toDouble()
        if (freqHz >= centerFreqsHz.last()) return gainsDb.last().toDouble()

        for (i in 0 until centerFreqsHz.size - 1) {
            val f0 = centerFreqsHz[i].toDouble()
            val f1 = centerFreqsHz[i + 1].toDouble()
            if (freqHz in f0..f1) {
                val t = (ln(freqHz) - ln(f0)) / (ln(f1) - ln(f0))
                return gainsDb[i] + t * (gainsDb[i + 1] - gainsDb[i])
            }
        }
        return gainsDb.last().toDouble()
    }
}
