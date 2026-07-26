package com.onyx.avhub.core.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.onyx.avhub.core.data.db.entity.EqPresetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EqPresetDao {
    @Query("SELECT * FROM eq_presets ORDER BY isBuiltIn DESC, name ASC")
    fun observeAll(): Flow<List<EqPresetEntity>>

    @Query("SELECT * FROM eq_presets WHERE id = :id")
    suspend fun getById(id: Long): EqPresetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(preset: EqPresetEntity): Long

    @Update
    suspend fun update(preset: EqPresetEntity)

    @Delete
    suspend fun delete(preset: EqPresetEntity)
}
