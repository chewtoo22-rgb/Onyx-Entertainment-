package com.onyx.avhub.feature.audiohub.tile

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.onyx.avhub.core.data.settings.SettingsRepository
import com.onyx.avhub.feature.audiohub.service.AudioHubService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Lets the user flip the system-wide audio hub on/off from the notification shade. */
@AndroidEntryPoint
class AudioHubQuickSettingsTileService : TileService() {

    @Inject lateinit var settingsRepository: SettingsRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onStartListening() {
        super.onStartListening()
        scope.launch { refreshTileState() }
    }

    override fun onClick() {
        super.onClick()
        scope.launch {
            val currentlyEnabled = settingsRepository.settings.first().systemAudioHubEnabled
            val nextEnabled = !currentlyEnabled
            settingsRepository.setSystemAudioHubEnabled(nextEnabled)
            if (nextEnabled) AudioHubService.start(this@AudioHubQuickSettingsTileService)
            refreshTileState()
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private suspend fun refreshTileState() {
        val enabled = settingsRepository.settings.first().systemAudioHubEnabled
        qsTile?.apply {
            state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            updateTile()
        }
    }
}
