package com.onyx.avhub.feature.player.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.onyx.avhub.core.data.settings.SettingsRepository
import com.onyx.avhub.core.media.codec.DecoderPreference
import com.onyx.avhub.core.media.player.OnyxPlayerFactory
import com.onyx.avhub.core.videogl.pipeline.VideoComparisonState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

data class PlayerUiState(
    val isPlaying: Boolean = false,
    val playbackState: Int = Player.STATE_IDLE,
    val decoderPreferences: Map<String, DecoderPreference> = emptyMap(),
    val splitPosition: Float = 0.5f,
    val sharpenStrength: Float = 0.6f,
    val hiResAudioEnabled: Boolean = false,
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext context: Context,
    playerFactory: OnyxPlayerFactory,
    private val settingsRepository: SettingsRepository,
    private val comparisonState: VideoComparisonState,
) : ViewModel() {

    private val decoderPreferences = mutableMapOf<String, DecoderPreference>()

    // DefaultRenderersFactory's audio sink config is fixed at ExoPlayer construction time and
    // can't be hot-swapped, so we read the persisted preference once up front; toggling it later
    // (see setHiResAudioEnabled) applies the next time a player is created rather than instantly.
    private val initialHiResAudioEnabled = runBlocking { settingsRepository.settings.first().hiResAudioEnabled }

    val player: ExoPlayer = playerFactory.create(
        context = context,
        decoderPreference = { mimeType -> decoderPreferences[mimeType] ?: DecoderPreference.AUTO },
        enableHiResFloatAudio = initialHiResAudioEnabled,
    )

    private val _uiState = MutableStateFlow(
        PlayerUiState(
            splitPosition = comparisonState.splitPosition,
            sharpenStrength = comparisonState.sharpenStrength,
            hiResAudioEnabled = initialHiResAudioEnabled,
        ),
    )
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.update { it.copy(isPlaying = isPlaying) }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _uiState.update { it.copy(playbackState = playbackState) }
            }
        })
    }

    fun playMedia(uri: Uri) {
        player.setMediaItem(MediaItem.fromUri(uri))
        player.prepare()
        player.playWhenReady = true
    }

    fun togglePlayPause() {
        player.playWhenReady = !player.playWhenReady
    }

    /**
     * Re-prepares the current item so [com.onyx.avhub.core.media.codec.OnyxMediaCodecSelector]
     * re-queries decoder candidates under the new preference; ExoPlayer only resolves decoders
     * when (re-)initializing a renderer, not on every preference change.
     */
    fun setDecoderPreference(mimeType: String, preference: DecoderPreference) {
        decoderPreferences[mimeType] = preference
        _uiState.update { it.copy(decoderPreferences = decoderPreferences.toMap()) }

        val currentItem = player.currentMediaItem ?: return
        val resumePositionMs = player.currentPosition
        player.setMediaItem(currentItem, resumePositionMs)
        player.prepare()
    }

    /** Drives the live before/after comparison shader; read every frame on the GL thread. */
    fun setSplitPosition(position: Float) {
        comparisonState.setSplitPosition(position)
        _uiState.update { it.copy(splitPosition = comparisonState.splitPosition) }
    }

    fun setSharpenStrength(strength: Float) {
        comparisonState.setSharpenStrength(strength)
        _uiState.update { it.copy(sharpenStrength = comparisonState.sharpenStrength) }
    }

    fun setHiResAudioEnabled(enabled: Boolean) {
        _uiState.update { it.copy(hiResAudioEnabled = enabled) }
        viewModelScope.launch { settingsRepository.setHiResAudioEnabled(enabled) }
    }

    override fun onCleared() {
        player.release()
        super.onCleared()
    }
}
