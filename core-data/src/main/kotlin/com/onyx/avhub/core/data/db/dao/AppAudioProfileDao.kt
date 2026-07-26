package com.onyx.avhub.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.onyx.avhub.core.data.db.entity.AppAudioProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppAudioProfileDao {
    @Query("SELECT * FROM app_audio_profiles")
    fun observeAll(): Flow<List<AppAudioProfileEntity>>

    @Query("SELECT * FROM app_audio_profiles WHERE packageName = :packageName")
    suspend fun getForPackage(packageName: String): AppAudioProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: AppAudioProfileEntity)

    @Query("DELETE FROM app_audio_profiles WHERE packageName = :packageName")
    suspend fun delete(packageName: String)
}
