package com.onyx.avhub.core.dsp

/**
 * JNI bridge to the native biquad cascade engine (`core-dsp/src/main/cpp`), used both by the
 * system-wide audio hub's custom-curve path and the in-app player's audio pipeline.
 *
 * [BiquadCoefficients][com.onyx.avhub.core.dsp.filter.BiquadCoefficients] computed in Kotlin are
 * pushed down per band via [setBand]; [process] runs the whole cascade over an interleaved PCM
 * float buffer in native code for real-time performance.
 */
class AudioDspEngine(sampleRateHz: Int, channelCount: Int, bandCount: Int) : AutoCloseable {

    private var nativeHandle: Long = nativeCreate(sampleRateHz, channelCount, bandCount)

    /** Pushes normalized biquad coefficients (a0 == 1) for one band into the native cascade. */
    fun setBand(bandIndex: Int, b0: Double, b1: Double, b2: Double, a1: Double, a2: Double) {
        checkNotReleased()
        nativeSetBand(nativeHandle, bandIndex, b0, b1, b2, a1, a2)
    }

    /** Processes [frameCount] interleaved frames of [buffer] in place. */
    fun process(buffer: FloatArray, frameCount: Int) {
        checkNotReleased()
        nativeProcess(nativeHandle, buffer, frameCount)
    }

    override fun close() {
        if (nativeHandle != 0L) {
            nativeDestroy(nativeHandle)
            nativeHandle = 0L
        }
    }

    private fun checkNotReleased() {
        check(nativeHandle != 0L) { "AudioDspEngine used after close()" }
    }

    private external fun nativeCreate(sampleRateHz: Int, channelCount: Int, bandCount: Int): Long
    private external fun nativeSetBand(
        handle: Long,
        bandIndex: Int,
        b0: Double,
        b1: Double,
        b2: Double,
        a1: Double,
        a2: Double,
    )
    private external fun nativeProcess(handle: Long, buffer: FloatArray, frameCount: Int)
    private external fun nativeDestroy(handle: Long)

    companion object {
        init {
            System.loadLibrary("onyx_dsp")
        }
    }
}
