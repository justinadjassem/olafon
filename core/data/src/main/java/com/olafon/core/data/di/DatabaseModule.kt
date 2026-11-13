package com.olafon.core.data.di

import android.content.Context
import androidx.room.Room
import com.olafon.core.data.db.OlafonDatabase
import com.olafon.core.data.db.dao.AlarmDao
import com.olafon.core.data.db.dao.QuestionDao
import com.olafon.core.data.db.dao.WakeSessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    /**
     * The database lives in device-protected storage so alarms can be read right after a reboot, before the user
     * unlocks the phone. Nothing sensitive beyond the user's own alarms and wake history is stored there.
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): OlafonDatabase {
        val storageContext = context.createDeviceProtectedStorageContext()
        return Room.databaseBuilder(storageContext, OlafonDatabase::class.java, OlafonDatabase.NAME).build()
    }

    @Provides
    fun provideAlarmDao(db: OlafonDatabase): AlarmDao = db.alarmDao()

    @Provides
    fun provideQuestionDao(db: OlafonDatabase): QuestionDao = db.questionDao()

    @Provides
    fun provideWakeSessionDao(db: OlafonDatabase): WakeSessionDao = db.wakeSessionDao()

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemDefaultZone()

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }
}
