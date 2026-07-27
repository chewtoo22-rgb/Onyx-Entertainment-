package com.onyx.avhub.feature.player.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.onyx.avhub.core.media.codec.DecoderPreference
import com.onyx.avhub.core.media.codec.OnyxCodecs

@Composable
fun PlayerScreen(viewModel: PlayerViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var isImmersive by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        ComparisonVideoSurface(
            player = viewModel.player,
            splitPosition = uiState.splitPosition,
            onSplitPositionChange = viewModel::setSplitPosition,
            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            PlaybackControls(
                isPlaying = uiState.isPlaying,
                onPlayPauseClick = viewModel::togglePlayPause,
            )
            ImmersiveToggleButton(
                isImmersive = isImmersive,
                onToggle = { isImmersive = !isImmersive },
            )
        }

        ComparisonControls(
            sharpenStrength = uiState.sharpenStrength,
            onSharpenStrengthChange = viewModel::setSharpenStrength,
        )

        OutputSettingsSection(
            hiResAudioEnabled = uiState.hiResAudioEnabled,
            onHiResAudioEnabledChange = viewModel::setHiResAudioEnabled,
        )

        CodecSettingsSection(
            decoderPreferences = uiState.decoderPreferences,
            onPreferenceChange = viewModel::setDecoderPreference,
        )
    }
}

/**
 * Renders the player with a live-draggable before/after divider on top: drag anywhere on the
 * video to move the split. Position is forwarded straight to [PlayerViewModel.setSplitPosition],
 * which the GL comparison shader reads every frame — no re-`prepare()` needed while dragging.
 */
@Composable
private fun ComparisonVideoSurface(
    player: ExoPlayer,
    splitPosition: Float,
    onSplitPositionChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var widthPx by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .onSizeChanged { widthPx = it.width.toFloat() }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    if (widthPx > 0f) {
                        onSplitPositionChange((change.position.x / widthPx).coerceIn(0f, 1f))
                    }
                }
            },
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                PlayerView(context).apply {
                    this.player = player
                    useController = true
                }
            },
        )

        val handleOffset = with(LocalDensity.current) { (widthPx * splitPosition).toDp() }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(2.dp)
                .offset(x = handleOffset)
                .background(Color.White.copy(alpha = 0.85f)),
        )
        Icon(
            imageVector = Icons.Filled.CompareArrows,
            contentDescription = "Drag to compare original vs. enhanced",
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = handleOffset - 12.dp)
                .size(24.dp),
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
private fun ComparisonControls(sharpenStrength: Float, onSharpenStrengthChange: (Float) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text("Enhancement Strength", style = MaterialTheme.typography.titleMedium)
        Text(
            "Right of the divider — real-time unsharp-mask sharpening",
            style = MaterialTheme.typography.bodyMedium,
        )
        Slider(value = sharpenStrength, onValueChange = onSharpenStrengthChange, valueRange = 0f..1f)
    }
}

@Composable
private fun OutputSettingsSection(hiResAudioEnabled: Boolean, onHiResAudioEnabledChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text("Hi-Res / Bit-Perfect Output", style = MaterialTheme.typography.titleMedium)
            Text(
                "Floating-point audio path, bypasses integer processing. Applies next playback.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Switch(checked = hiResAudioEnabled, onCheckedChange = onHiResAudioEnabledChange)
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
