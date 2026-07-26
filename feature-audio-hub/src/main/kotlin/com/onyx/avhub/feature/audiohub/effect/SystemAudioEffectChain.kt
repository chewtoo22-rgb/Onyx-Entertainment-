package com.onyx.avhub.feature.audiohub.effect

import android.media.audiofx.BassBoost
import android.media.audiofx.DynamicsProcessing
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.onyx.avhub.core.data.db.entity.EqPresetEntity
import com.onyx.avhub.core.dsp.filter.EqCurveInterpolator

private const val TAG = "SystemAudioEffectChain"
private const val DEFAULT_PRIORITY = 0

/**
 * Attaches the platform's `android.media.audiofx` effects to the global output mix
 * (audio session 0), which is how a normal (non-root) app enhances audio coming from
 * every other app on the device — the same mechanism Wavelet/Poweramp rely on.
 *
 * Each effect is constructed defensively: device/OEM audio HALs vary in what they actually
 * support on session 0, so a failure to create or configure one effect must not take down
 * the rest of the chain.
 */
class SystemAudioEffectChain(private val audioSessionId: Int = GLOBAL_MIX_SESSION_ID) {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var presetReverb: PresetReverb? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var dynamicsProcessing: DynamicsProcessing? = null

    val isAttached: Boolean
        get() = equalizer != null

    fun attach() {
        equalizer = createEffect("Equalizer") { Equalizer(DEFAULT_PRIORITY, audioSessionId) }
        bassBoost = createEffect("BassBoost") { BassBoost(DEFAULT_PRIORITY, audioSessionId) }
        virtualizer = createEffect("Virtualizer") { Virtualizer(DEFAULT_PRIORITY, audioSessionId) }
        presetReverb = createEffect("PresetReverb") { PresetReverb(DEFAULT_PRIORITY, audioSessionId) }
        loudnessEnhancer = createEffect("LoudnessEnhancer") { LoudnessEnhancer(audioSessionId) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            dynamicsProcessing = createLimiterOnlyDynamicsProcessing()
        }
    }

    fun setEnabled(enabled: Boolean) {
        // AudioEffect.setEnabled(boolean) returns a status int, not Unit, so it can't be
        // exposed as a Kotlin `var` property (only assignment syntax requires a Unit setter).
        equalizer?.setEnabled(enabled)
        bassBoost?.setEnabled(enabled)
        virtualizer?.setEnabled(enabled)
        presetReverb?.setEnabled(enabled)
        loudnessEnhancer?.setEnabled(enabled)
        dynamicsProcessing?.setEnabled(enabled)
    }

    fun applyPreset(preset: EqPresetEntity) {
        applyEqualizerCurve(preset.bandCenterFreqHz, preset.bandGainsDb)

        bassBoost?.let { boost ->
            if (boost.strengthSupported) {
                runCatching { boost.setStrength(preset.bassBoostStrength.toShort()) }
            }
        }
        virtualizer?.let { virt ->
            if (virt.strengthSupported) {
                runCatching { virt.setStrength(preset.virtualizerStrength.toShort()) }
            }
        }
        presetReverb?.let { reverb ->
            runCatching { reverb.setPreset(preset.reverbPreset.toShort()) }
        }
    }

    /** Resamples the preset's generic curve onto this device's actual Equalizer band layout. */
    private fun applyEqualizerCurve(centerFreqsHz: List<Int>, gainsDb: List<Float>) {
        val eq = equalizer ?: return
        runCatching {
            val range = eq.bandLevelRange
            val minMillibel = range[0].toInt()
            val maxMillibel = range[1].toInt()
            for (band in 0 until eq.numberOfBands.toInt()) {
                val bandIndex = band.toShort()
                val centerFreqHz = eq.getCenterFreq(bandIndex) / 1000.0
                val gainDb = EqCurveInterpolator.gainAtHz(centerFreqHz, centerFreqsHz, gainsDb)
                val millibel = (gainDb * 100.0).toInt().coerceIn(minMillibel, maxMillibel)
                eq.setBandLevel(bandIndex, millibel.toShort())
            }
        }.onFailure { Log.w(TAG, "Failed to apply equalizer curve", it) }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun createLimiterOnlyDynamicsProcessing(): DynamicsProcessing? = runCatching {
        val channelCount = 2
        val config = DynamicsProcessing.Config.Builder(
            DynamicsProcessing.VARIANT_FAVOR_FREQUENCY_RESOLUTION,
            channelCount,
            /* preEqInUse= */ false, /* preEqBandCount= */ 0,
            /* mbcInUse= */ false, /* mbcBandCount= */ 0,
            /* postEqInUse= */ false, /* postEqBandCount= */ 0,
            /* limiterInUse= */ true,
        ).build()

        val dp = DynamicsProcessing(DEFAULT_PRIORITY, audioSessionId, config)
        val limiter = DynamicsProcessing.Limiter(
            /* enabled= */ true,
            /* useInputGain= */ true,
            /* linkGroup= */ 0,
            /* attackTime= */ 3f,
            /* releaseTime= */ 50f,
            /* ratio= */ 10f,
            /* threshold= */ -1f,
            /* postGain= */ 0f,
        )
        for (channel in 0 until channelCount) {
            dp.setLimiterByChannelIndex(channel, limiter)
        }
        dp
    }.onFailure { Log.w(TAG, "DynamicsProcessing limiter unavailable on this device", it) }
        .getOrNull()

    fun release() {
        runCatching { equalizer?.release() }
        runCatching { bassBoost?.release() }
        runCatching { virtualizer?.release() }
        runCatching { presetReverb?.release() }
        runCatching { loudnessEnhancer?.release() }
        runCatching { dynamicsProcessing?.release() }
        equalizer = null
        bassBoost = null
        virtualizer = null
        presetReverb = null
        loudnessEnhancer = null
        dynamicsProcessing = null
    }

    private inline fun <T> createEffect(name: String, factory: () -> T): T? =
        runCatching(factory).onFailure { Log.w(TAG, "$name unsupported on session $audioSessionId", it) }
            .getOrNull()

    companion object {
        /** Audio session 0 is the device's global output mix — attaching here applies the
         *  effect to every app's audio, not just ours. */
        const val GLOBAL_MIX_SESSION_ID = 0
    }
}
