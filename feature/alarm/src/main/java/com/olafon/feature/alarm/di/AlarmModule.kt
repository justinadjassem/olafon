package com.olafon.feature.alarm.di

import com.olafon.core.domain.scheduler.AlarmScheduler
import com.olafon.feature.alarm.scheduler.AndroidAlarmScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AlarmModule {
    @Binds
    abstract fun bindAlarmScheduler(impl: AndroidAlarmScheduler): AlarmScheduler
}
