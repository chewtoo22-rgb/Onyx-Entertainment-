package com.onyx.avhub.core.data.db.entity

import androidx.room.Entity

/** Binds a package name to a preset so the audio hub can auto-switch per foreground app. */
@Entity(tableName = "app_audio_profiles", primaryKeys = ["packageName"])
data class AppAudioProfileEntity(
    val packageName: String,
    val presetId: Long,
    val enabled: Boolean = true,
)
