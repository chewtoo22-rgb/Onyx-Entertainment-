package com.onyx.avhub.feature.audiohub.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onyx.avhub.core.data.db.entity.EqPresetEntity
import com.onyx.avhub.core.data.repository.PresetRepository
import com.onyx.avhub.core.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

@HiltViewModel
class AudioHubViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val presetRepository: PresetRepository,
) : ViewModel() {

    init {
        viewModelScope.launch { presetRepository.ensureBuiltInPresetsSeeded() }
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
}
