package com.onyx.avhub.core.common.util

import kotlin.math.ln
import kotlin.math.pow

/** Shared amplitude/decibel conversions used by both the DSP engine and UI meters. */
object AudioMath {
    fun linearToDb(linear: Double): Double =
        if (linear <= 0.0) Double.NEGATIVE_INFINITY else 20.0 * ln(linear) / ln(10.0)

    fun dbToLinear(db: Double): Double = 10.0.pow(db / 20.0)

    fun clampDb(db: Double, min: Double = -24.0, max: Double = 24.0): Double =
        db.coerceIn(min, max)
}
