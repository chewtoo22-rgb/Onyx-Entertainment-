package com.onyx.avhub.feature.player.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.onyx.avhub.core.media.codec.DecoderPreference
import com.onyx.avhub.core.media.player.OnyxPlayerFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PlayerUiState(
    val isPlaying: Boolean = false,
    val playbackState: Int = Player.STATE_IDLE,
    val decoderPreferences: Map<String, DecoderPreference> = emptyMap(),
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext context: Context,
    playerFactory: OnyxPlayerFactory,
) : ViewModel() {

    private val decoderPreferences = mutableMapOf<String, DecoderPreference>()

    val player: ExoPlayer = playerFactory.create(
        context = context,
        decoderPreference = { mimeType -> decoderPreferences[mimeType] ?: DecoderPreference.AUTO },
    )

    private val _uiState = MutableStateFlow(PlayerUiState())
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

    override fun onCleared() {
        player.release()
        super.onCleared()
    }
}
