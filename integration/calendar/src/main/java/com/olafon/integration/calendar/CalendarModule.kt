package com.olafon.integration.calendar

import com.olafon.core.domain.briefing.ScheduleSource
import com.olafon.core.domain.repository.CalendarRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class CalendarModule {
    @Binds
    abstract fun bindCalendarRepository(impl: DeviceCalendarRepository): CalendarRepository

    @Binds
    @IntoSet
    abstract fun bindDeviceCalendarSource(impl: DeviceCalendarSource): ScheduleSource
}
