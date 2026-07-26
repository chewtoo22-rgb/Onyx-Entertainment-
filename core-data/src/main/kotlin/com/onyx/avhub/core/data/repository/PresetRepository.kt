package com.onyx.avhub.core.data.repository

import com.onyx.avhub.core.data.db.dao.EqPresetDao
import com.onyx.avhub.core.data.db.entity.EqPresetEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class PresetRepository @Inject constructor(
    private val eqPresetDao: EqPresetDao,
) {
    fun observePresets(): Flow<List<EqPresetEntity>> = eqPresetDao.observeAll()

    suspend fun getPreset(id: Long): EqPresetEntity? = eqPresetDao.getById(id)

    suspend fun savePreset(preset: EqPresetEntity): Long = eqPresetDao.upsert(preset)

    suspend fun deletePreset(preset: EqPresetEntity) {
        check(!preset.isBuiltIn) { "Built-in presets cannot be deleted" }
        eqPresetDao.delete(preset)
    }

    suspend fun ensureBuiltInPresetsSeeded() {
        BuiltInPresets.ALL.forEach { eqPresetDao.upsert(it) }
    }
}
