package com.onyx.avhub.feature.audiohub.effect

import android.media.audiofx.Visualizer
import android.util.Log
import com.onyx.avhub.core.dsp.filter.SpectrumAnalyzer
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "SystemAudioVisualizer"
private const val VISUAL_BAND_COUNT = 32

/**
 * Captures FFT data from the global mix (session 0, same pattern as [SystemAudioEffectChain])
 * via the platform [Visualizer] API and exposes it as normalized visual bands for
 * [com.onyx.avhub.feature.audiohub.ui.SpectrumVisualizerView].
 *
 * Unlike the [android.media.audiofx.AudioEffect] subclasses in [SystemAudioEffectChain],
 * `Visualizer` requires `android.permission.RECORD_AUDIO` even at session 0 (per its class
 * Javadoc: "to protect privacy of certain audio data... the use of the visualizer requires
 * the permission"). Callers must confirm that permission is granted before calling [start];
 * this class does not request it itself.
 *
 * Also defensive: OEM audio HALs vary in what they allow on session 0, so any other failure
 * degrades to an all-zero (idle) spectrum rather than crashing the Audio Hub screen.
 */
class SystemAudioVisualizerCapture @Inject constructor() {

    private var visualizer: Visualizer? = null

    private val _bands = MutableStateFlow(FloatArray(VISUAL_BAND_COUNT))
    val bands: StateFlow<FloatArray> = _bands.asStateFlow()

    fun start() {
        if (visualizer != null) return
        var openedVisualizer: Visualizer? = null
        runCatching {
            val captureSize = Visualizer.getCaptureSizeRange()[1]
            Visualizer(SystemAudioEffectChain.GLOBAL_MIX_SESSION_ID).also { openedVisualizer = it }.apply {
                setCaptureSize(captureSize)
                setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(v: Visualizer?, waveform: ByteArray?, rate: Int) = Unit

                        override fun onFftDataCapture(v: Visualizer?, fft: ByteArray?, samplingRateMilliHz: Int) {
                            if (fft == null) return
                            val magnitudes = SpectrumAnalyzer.magnitudesFromFft(fft)
                            _bands.value = SpectrumAnalyzer.bucketToBands(
                                magnitudes,
                                sampleRateHz = samplingRateMilliHz / 1000,
                                bandCount = VISUAL_BAND_COUNT,
                            )
                        }
                    },
                    Visualizer.getMaxCaptureRate() / 2,
                    /* waveform= */ false,
                    /* fft= */ true,
                )
                setEnabled(true)
                visualizer = this
            }
        }.onFailure {
            // Partial setup (e.g. setCaptureSize/listener/setEnabled threw after construction)
            // would otherwise leak the native Visualizer until process exit.
            runCatching { openedVisualizer?.release() }
            Log.w(TAG, "System audio visualizer capture unavailable on this device", it)
        }
    }

    fun stop() {
        runCatching { visualizer?.release() }
        visualizer = null
        _bands.value = FloatArray(VISUAL_BAND_COUNT)
    }
}
