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
    @ApplicationContext private val context: Context,
    private val playerFactory: OnyxPlayerFactory,
    private val settingsRepository: SettingsRepository,
    private val comparisonState: VideoComparisonState,
) : ViewModel() {

    private val decoderPreferences = mutableMapOf<String, DecoderPreference>()

    // Built with a safe default so construction never blocks on a DataStore read; the persisted
    // preference (if different) is applied asynchronously right after via applyHiResAudioSetting,
    // which also backs setHiResAudioEnabled — DefaultRenderersFactory's audio sink config is
    // fixed at ExoPlayer construction time, so "applying" the setting means swapping the player.
    private var player: ExoPlayer = createPlayer(enableHiResFloatAudio = false)

    private val _playerState = MutableStateFlow(player)
    val playerState: StateFlow<ExoPlayer> = _playerState.asStateFlow()

    private val _uiState = MutableStateFlow(
        PlayerUiState(
            splitPosition = comparisonState.splitPosition,
            sharpenStrength = comparisonState.sharpenStrength,
        ),
    )
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        attachListeners(player)
        viewModelScope.launch {
            val persistedHiRes = settingsRepository.settings.first().hiResAudioEnabled
            if (persistedHiRes) applyHiResAudioSetting(persistedHiRes)
        }
    }

    private fun createPlayer(enableHiResFloatAudio: Boolean): ExoPlayer = playerFactory.create(
        context = context,
        decoderPreference = { mimeType -> decoderPreferences[mimeType] ?: DecoderPreference.AUTO },
        enableHiResFloatAudio = enableHiResFloatAudio,
    )

    private fun attachListeners(target: ExoPlayer) {
        target.addListener(object : Player.Listener {
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
        viewModelScope.launch { settingsRepository.setHiResAudioEnabled(enabled) }
        applyHiResAudioSetting(enabled)
    }

    /**
     * Media3's float-output path can only be configured when the [ExoPlayer] is built, so
     * actually applying a toggle means building a replacement player carrying over the current
     * item/position/play-state, publishing it via [playerState] so the UI rebinds
     * `PlayerView.player`, and releasing the old one only once the new one is ready.
     */
    private fun applyHiResAudioSetting(enabled: Boolean) {
        if (_uiState.value.hiResAudioEnabled == enabled) return

        val previousPlayer = player
        val currentItem = previousPlayer.currentMediaItem
        val resumePositionMs = previousPlayer.currentPosition
        val wasPlaying = previousPlayer.isPlaying

        val newPlayer = createPlayer(enableHiResFloatAudio = enabled)
        attachListeners(newPlayer)
        if (currentItem != null) {
            newPlayer.setMediaItem(currentItem, resumePositionMs)
            newPlayer.prepare()
            newPlayer.playWhenReady = wasPlaying
        }

        player = newPlayer
        _playerState.value = newPlayer
        previousPlayer.release()
        _uiState.update { it.copy(hiResAudioEnabled = enabled) }
    }

    override fun onCleared() {
        player.release()
        super.onCleared()
    }
}
