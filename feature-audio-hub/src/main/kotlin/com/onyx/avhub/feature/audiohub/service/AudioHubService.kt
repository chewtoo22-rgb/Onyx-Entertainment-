package com.onyx.avhub.feature.audiohub.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.onyx.avhub.core.data.repository.PresetRepository
import com.onyx.avhub.core.data.settings.SettingsRepository
import com.onyx.avhub.feature.audiohub.R
import com.onyx.avhub.feature.audiohub.effect.SystemAudioEffectChain
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Foreground service that keeps [SystemAudioEffectChain] attached to the global audio
 * session for as long as the hub is enabled, so enhancement keeps applying to every app's
 * audio even while Onyx itself isn't in the foreground.
 */
@AndroidEntryPoint
class AudioHubService : LifecycleService() {

    @Inject lateinit var settingsRepository: SettingsRepository

    @Inject lateinit var presetRepository: PresetRepository

    private val effectChain = SystemAudioEffectChain()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(enhancementActive = false),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
        )
        effectChain.attach()

        lifecycleScope.launch {
            settingsRepository.settings.collectLatest { settings ->
                effectChain.setEnabled(settings.systemAudioHubEnabled)
                if (settings.systemAudioHubEnabled) {
                    presetRepository.getPreset(settings.activePresetId)?.let(effectChain::applyPreset)
                }
                updateNotification(settings.systemAudioHubEnabled)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onDestroy() {
        effectChain.release()
        super.onDestroy()
    }

    private fun updateNotification(enhancementActive: Boolean) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(enhancementActive))
    }

    private fun buildNotification(enhancementActive: Boolean): Notification =
        NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.audio_hub_notification_title))
            .setContentText(
                getString(
                    if (enhancementActive) {
                        R.string.audio_hub_notification_text_on
                    } else {
                        R.string.audio_hub_notification_text_off
                    },
                ),
            )
            .setSmallIcon(R.drawable.ic_audio_hub_tile)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            getString(R.string.audio_hub_notification_channel_name),
            NotificationManager.IMPORTANCE_MIN,
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val NOTIFICATION_CHANNEL_ID = "audio_hub"

        fun start(context: Context) {
            context.startForegroundService(Intent(context, AudioHubService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, AudioHubService::class.java))
        }
    }
}
