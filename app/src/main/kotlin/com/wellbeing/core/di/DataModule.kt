package com.wellbeing.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.wellbeing.core.data.local.WellbeingDatabase
import com.wellbeing.core.data.local.dao.WellbeingDao
import com.wellbeing.core.data.repository.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WellbeingDatabase {
        return Room.databaseBuilder(
            context,
            WellbeingDatabase::class.java,
            "wellbeing.db"
        )
            .fallbackToDestructiveMigration()
            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
            .build()
    }

    @Provides
    fun provideWellbeingDao(database: WellbeingDatabase): WellbeingDao {
        return database.wellbeingDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUsageRepository(
        usageRepositoryImpl: UsageRepositoryImpl
    ): UsageRepository

    @Binds
    @Singleton
    abstract fun bindFocusModeRepository(
        focusModeRepositoryImpl: FocusModeRepositoryImpl
    ): FocusModeRepository

    @Binds
    @Singleton
    abstract fun bindBedtimeRepository(
        bedtimeRepositoryImpl: BedtimeRepositoryImpl
    ): BedtimeRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository
}
