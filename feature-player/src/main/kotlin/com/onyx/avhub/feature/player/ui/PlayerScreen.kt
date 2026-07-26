package com.onyx.avhub.feature.player.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.ui.PlayerView
import com.onyx.avhub.core.media.codec.DecoderPreference
import com.onyx.avhub.core.media.codec.OnyxCodecs

@Composable
fun PlayerScreen(viewModel: PlayerViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxWidth()) {
        AndroidView(
            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
            factory = { context ->
                PlayerView(context).apply {
                    player = viewModel.player
                    useController = true
                }
            },
        )

        PlaybackControls(
            isPlaying = uiState.isPlaying,
            onPlayPauseClick = viewModel::togglePlayPause,
        )

        CodecSettingsSection(
            decoderPreferences = uiState.decoderPreferences,
            onPreferenceChange = viewModel::setDecoderPreference,
        )
    }
}

@Composable
private fun PlaybackControls(isPlaying: Boolean, onPlayPauseClick: () -> Unit) {
    FilledIconButton(onClick = onPlayPauseClick, modifier = Modifier.padding(16.dp)) {
        Icon(
            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
        )
    }
}

@Composable
private fun CodecSettingsSection(
    decoderPreferences: Map<String, DecoderPreference>,
    onPreferenceChange: (mimeType: String, DecoderPreference) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text("Codec Settings", style = MaterialTheme.typography.titleLarge)

        Text(
            "Video",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 12.dp),
        )
        CodecPreferenceList(OnyxCodecs.VIDEO, decoderPreferences, onPreferenceChange)

        Text(
            "Audio",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 12.dp),
        )
        CodecPreferenceList(OnyxCodecs.AUDIO, decoderPreferences, onPreferenceChange)
    }
}

@Composable
private fun CodecPreferenceList(
    codecs: Map<String, String>,
    decoderPreferences: Map<String, DecoderPreference>,
    onPreferenceChange: (mimeType: String, DecoderPreference) -> Unit,
) {
    LazyColumn {
        items(codecs.entries.toList()) { (mimeType, label) ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(label, style = MaterialTheme.typography.labelLarge)
                    LazyRow(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(DecoderPreference.entries) { preference ->
                            val selected = (decoderPreferences[mimeType] ?: DecoderPreference.AUTO) == preference
                            FilterChip(
                                selected = selected,
                                onClick = { onPreferenceChange(mimeType, preference) },
                                label = { Text(preference.name.replace('_', ' ')) },
                            )
                        }
                    }
                }
            }
        }
    }
}
