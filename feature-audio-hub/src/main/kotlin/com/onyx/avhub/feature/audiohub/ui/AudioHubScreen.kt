package com.onyx.avhub.feature.audiohub.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.onyx.avhub.core.data.db.entity.EqPresetEntity

private const val BASS_BAND_INDEX = 1

@Composable
fun AudioHubScreen(viewModel: AudioHubViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val spectrumBands by viewModel.spectrumBands.collectAsState()
    val context = LocalContext.current

    var hasVisualizerPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasVisualizerPermission = granted
    }

    // The Visualizer (unlike the Equalizer/BassBoost/etc. effect chain) requires RECORD_AUDIO
    // even at session 0, so capture can only run once both the hub is on and that's granted;
    // turning the hub off must stop capture, not just hide its UI.
    LaunchedEffect(uiState.hubEnabled, hasVisualizerPermission) {
        viewModel.setVisualizerCaptureActive(uiState.hubEnabled && hasVisualizerPermission)
    }

    AudioHubContent(
        uiState = uiState,
        spectrumBands = spectrumBands,
        showVisualizerPermissionRequest = uiState.hubEnabled && !hasVisualizerPermission,
        onRequestVisualizerPermission = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
        onHubEnabledChange = viewModel::setHubEnabled,
        onPresetSelected = viewModel::selectPreset,
        onBandGainChange = { bandIndex, gainDb ->
            uiState.activePreset?.let { viewModel.updateBandGain(it, bandIndex, gainDb) }
        },
    )
}

@Composable
private fun AudioHubContent(
    uiState: AudioHubUiState,
    spectrumBands: FloatArray,
    showVisualizerPermissionRequest: Boolean,
    onRequestVisualizerPermission: () -> Unit,
    onHubEnabledChange: (Boolean) -> Unit,
    onPresetSelected: (Long) -> Unit,
    onBandGainChange: (bandIndex: Int, gainDb: Float) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("System-Wide Audio Hub", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Enhances audio from every app on this device",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Switch(checked = uiState.hubEnabled, onCheckedChange = onHubEnabledChange)
        }

        if (showVisualizerPermissionRequest) {
            VisualizerPermissionBanner(
                onRequestPermission = onRequestVisualizerPermission,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            )
        }

        Box(modifier = Modifier.fillMaxWidth().height(160.dp).padding(top = 16.dp)) {
            AmbientGlow(
                bassIntensity = spectrumBands.getOrElse(BASS_BAND_INDEX) { 0f },
                modifier = Modifier.fillMaxWidth().height(160.dp),
            )
            SpectrumVisualizerView(
                bands = spectrumBands,
                modifier = Modifier.fillMaxWidth().height(160.dp).padding(horizontal = 8.dp),
            )
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(uiState.presets, key = { it.id }) { preset ->
                FilterChip(
                    selected = preset.id == uiState.activePresetId,
                    onClick = { onPresetSelected(preset.id) },
                    label = { Text(preset.name) },
                )
            }
        }

        uiState.activePreset?.let { preset ->
            EqualizerCurveEditor(
                preset = preset,
                onBandGainChange = onBandGainChange,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            )
        }
    }
}

@Composable
private fun VisualizerPermissionBanner(onRequestPermission: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Enable the spectrum visualizer", style = MaterialTheme.typography.titleMedium)
            Text(
                "The platform requires audio-capture permission to draw the live spectrum — " +
                    "audio is analyzed for the animation only, never recorded or stored.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
            )
            Button(onClick = onRequestPermission) { Text("Grant permission") }
        }
    }
}

@Composable
private fun EqualizerCurveEditor(
    preset: EqPresetEntity,
    onBandGainChange: (bandIndex: Int, gainDb: Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            items(preset.bandCenterFreqHz.indices.toList()) { bandIndex ->
                EqBandRow(
                    frequencyHz = preset.bandCenterFreqHz[bandIndex],
                    gainDb = preset.bandGainsDb[bandIndex],
                    onGainChange = { onBandGainChange(bandIndex, it) },
                )
            }
        }
    }
}

@Composable
private fun EqBandRow(frequencyHz: Int, gainDb: Float, onGainChange: (Float) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(formatFrequencyLabel(frequencyHz), style = MaterialTheme.typography.labelLarge)
            Text("%+.1f dB".format(gainDb), style = MaterialTheme.typography.labelLarge)
        }
        Slider(
            value = gainDb,
            onValueChange = onGainChange,
            valueRange = -12f..12f,
        )
    }
}

private fun formatFrequencyLabel(hz: Int): String =
    if (hz >= 1000) "${hz / 1000}kHz" else "${hz}Hz"
