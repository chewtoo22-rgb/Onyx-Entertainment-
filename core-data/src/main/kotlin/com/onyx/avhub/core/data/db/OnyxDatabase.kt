package com.onyx.avhub.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.onyx.avhub.core.data.db.dao.AppAudioProfileDao
import com.onyx.avhub.core.data.db.dao.EqPresetDao
import com.onyx.avhub.core.data.db.entity.AppAudioProfileEntity
import com.onyx.avhub.core.data.db.entity.EqPresetEntity

@Database(
    entities = [EqPresetEntity::class, AppAudioProfileEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class OnyxDatabase : RoomDatabase() {
    abstract fun eqPresetDao(): EqPresetDao
    abstract fun appAudioProfileDao(): AppAudioProfileDao

    companion object {
        const val DATABASE_NAME = "onyx_av_hub.db"
    }
}
