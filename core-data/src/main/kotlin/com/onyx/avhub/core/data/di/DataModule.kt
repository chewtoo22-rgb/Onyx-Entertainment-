package com.onyx.avhub.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.onyx.avhub.core.data.db.OnyxDatabase
import com.onyx.avhub.core.data.db.dao.AppAudioProfileDao
import com.onyx.avhub.core.data.db.dao.EqPresetDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "onyx_settings")

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): OnyxDatabase =
        Room.databaseBuilder(context, OnyxDatabase::class.java, OnyxDatabase.DATABASE_NAME)
            .build()

    @Provides
    fun provideEqPresetDao(database: OnyxDatabase): EqPresetDao = database.eqPresetDao()

    @Provides
    fun provideAppAudioProfileDao(database: OnyxDatabase): AppAudioProfileDao =
        database.appAudioProfileDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore
}
