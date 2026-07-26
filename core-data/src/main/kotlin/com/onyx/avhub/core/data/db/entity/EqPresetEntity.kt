package com.onyx.avhub.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A saved equalizer curve, usable by both the system-wide audio hub and the
 * in-app player's audio path.
 *
 * [bandGainsDb] holds one gain value per band, matching [bandCenterFreqHz] index
 * for index; band layout is variable (5/10/15/31) so both arrays travel together.
 */
@Entity(tableName = "eq_presets")
data class EqPresetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val bandCenterFreqHz: List<Int>,
    val bandGainsDb: List<Float>,
    val preampDb: Float = 0f,
    val bassBoostStrength: Int = 0,
    val virtualizerStrength: Int = 0,
    val reverbPreset: Int = 0,
    val isBuiltIn: Boolean = false,
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
)
