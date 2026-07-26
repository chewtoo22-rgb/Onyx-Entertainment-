package com.onyx.avhub.core.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private object Keys {
    val SYSTEM_AUDIO_HUB_ENABLED = booleanPreferencesKey("system_audio_hub_enabled")
    val ACTIVE_PRESET_ID = longPreferencesKey("active_preset_id")
    val PER_APP_PROFILES_ENABLED = booleanPreferencesKey("per_app_profiles_enabled")
    val LOUDNESS_NORMALIZATION_ENABLED = booleanPreferencesKey("loudness_normalization_enabled")
    val VIDEO_QUALITY_MODE = stringPreferencesKey("video_quality_mode")
    val PREFER_HARDWARE_DECODING = booleanPreferencesKey("prefer_hardware_decoding")
    val HDR_TONE_MAPPING_ENABLED = booleanPreferencesKey("hdr_tone_mapping_enabled")
}

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val settings: Flow<OnyxSettings> = dataStore.data.map { prefs ->
        OnyxSettings(
            systemAudioHubEnabled = prefs[Keys.SYSTEM_AUDIO_HUB_ENABLED] ?: false,
            activePresetId = prefs[Keys.ACTIVE_PRESET_ID] ?: 1L,
            perAppProfilesEnabled = prefs[Keys.PER_APP_PROFILES_ENABLED] ?: false,
            loudnessNormalizationEnabled = prefs[Keys.LOUDNESS_NORMALIZATION_ENABLED] ?: false,
            videoQualityMode = prefs[Keys.VIDEO_QUALITY_MODE]
                ?.let { runCatching { VideoQualityMode.valueOf(it) }.getOrNull() }
                ?: VideoQualityMode.BALANCED,
            preferHardwareDecoding = prefs[Keys.PREFER_HARDWARE_DECODING] ?: true,
            hdrToneMappingEnabled = prefs[Keys.HDR_TONE_MAPPING_ENABLED] ?: true,
        )
    }

    suspend fun setSystemAudioHubEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.SYSTEM_AUDIO_HUB_ENABLED] = enabled }
    }

    suspend fun setActivePresetId(presetId: Long) {
        dataStore.edit { it[Keys.ACTIVE_PRESET_ID] = presetId }
    }

    suspend fun setPerAppProfilesEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.PER_APP_PROFILES_ENABLED] = enabled }
    }

    suspend fun setLoudnessNormalizationEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.LOUDNESS_NORMALIZATION_ENABLED] = enabled }
    }

    suspend fun setVideoQualityMode(mode: VideoQualityMode) {
        dataStore.edit { it[Keys.VIDEO_QUALITY_MODE] = mode.name }
    }

    suspend fun setPreferHardwareDecoding(enabled: Boolean) {
        dataStore.edit { it[Keys.PREFER_HARDWARE_DECODING] = enabled }
    }

    suspend fun setHdrToneMappingEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.HDR_TONE_MAPPING_ENABLED] = enabled }
    }
}
