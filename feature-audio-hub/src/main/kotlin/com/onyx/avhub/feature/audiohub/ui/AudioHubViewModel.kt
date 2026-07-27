package com.onyx.avhub.feature.audiohub.ui

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onyx.avhub.core.data.db.entity.EqPresetEntity
import com.onyx.avhub.core.data.repository.PresetRepository
import com.onyx.avhub.core.data.settings.SettingsRepository
import com.onyx.avhub.feature.audiohub.effect.SystemAudioVisualizerCapture
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AudioHubUiState(
    val hubEnabled: Boolean = false,
    val presets: List<EqPresetEntity> = emptyList(),
    val activePresetId: Long = 1L,
) {
    val activePreset: EqPresetEntity?
        get() = presets.find { it.id == activePresetId } ?: presets.firstOrNull()
}

private const val BASS_BAND_INDEX = 1
private const val BASS_HIT_THRESHOLD = 0.82f
private const val BASS_HIT_COOLDOWN_MS = 180L
private const val HAPTIC_PULSE_MS = 18L

@HiltViewModel
class AudioHubViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val presetRepository: PresetRepository,
    private val visualizerCapture: SystemAudioVisualizerCapture,
) : ViewModel() {

    private val vibrator: Vibrator? = context.getSystemService(Vibrator::class.java)
    private var lastHapticPulseAtMs = 0L
    private var isCaptureActive = false

    val spectrumBands: StateFlow<FloatArray> = visualizerCapture.bands

    init {
        viewModelScope.launch { presetRepository.ensureBuiltInPresetsSeeded() }
        viewModelScope.launch {
            visualizerCapture.bands.collect { bands -> maybePulseOnBassHit(bands) }
        }
    }

    val uiState: StateFlow<AudioHubUiState> = combine(
        settingsRepository.settings,
        presetRepository.observePresets(),
    ) { settings, presets ->
        AudioHubUiState(
            hubEnabled = settings.systemAudioHubEnabled,
            presets = presets,
            activePresetId = settings.activePresetId,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AudioHubUiState())

    fun setHubEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setSystemAudioHubEnabled(enabled) }
    }

    /**
     * Starts/stops the [Visualizer][android.media.audiofx.Visualizer] capture. The caller
     * (the Compose screen) is responsible for only passing `true` when both the hub is enabled
     * and `RECORD_AUDIO` has actually been granted — disabling the hub must stop capture (and
     * with it, the bass-triggered haptics), not just hide the UI for it.
     */
    fun setVisualizerCaptureActive(active: Boolean) {
        if (active == isCaptureActive) return
        isCaptureActive = active
        if (active) visualizerCapture.start() else visualizerCapture.stop()
    }

    fun selectPreset(presetId: Long) {
        viewModelScope.launch { settingsRepository.setActivePresetId(presetId) }
    }

    fun updateBandGain(preset: EqPresetEntity, bandIndex: Int, gainDb: Float) {
        viewModelScope.launch {
            val updatedGains = preset.bandGainsDb.toMutableList().apply { this[bandIndex] = gainDb }
            val customized = if (preset.isBuiltIn) {
                preset.copy(id = 0, name = "Custom", isBuiltIn = false, bandGainsDb = updatedGains)
            } else {
                preset.copy(bandGainsDb = updatedGains)
            }
            val savedId = presetRepository.savePreset(customized)
            settingsRepository.setActivePresetId(savedId)
        }
    }

    /** Fires a short haptic pulse on strong bass hits, throttled so it reads as a beat, not a buzz. */
    private fun maybePulseOnBassHit(bands: FloatArray) {
        if (bands.size <= BASS_BAND_INDEX || bands[BASS_BAND_INDEX] < BASS_HIT_THRESHOLD) return
        val now = System.currentTimeMillis()
        if (now - lastHapticPulseAtMs < BASS_HIT_COOLDOWN_MS) return
        lastHapticPulseAtMs = now
        vibrator?.vibrate(VibrationEffect.createOneShot(HAPTIC_PULSE_MS, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    override fun onCleared() {
        setVisualizerCaptureActive(false)
        super.onCleared()
    }
}
