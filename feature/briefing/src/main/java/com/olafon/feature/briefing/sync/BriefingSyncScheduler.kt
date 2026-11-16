package com.olafon.feature.briefing.sync

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.olafon.core.domain.repository.PreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Clock
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/** Arms the daily evening sync. No network constraint: the phone's calendars are read locally. */
class BriefingSyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: PreferencesRepository,
    private val clock: Clock,
) {
    /** Safe to call at every app start: the periodic work is updated in place, never duplicated. */
    suspend fun schedule() {
        val hour = preferences.observe().first().eveningSyncHour
        val now = ZonedDateTime.now(clock)
        val next = now.withHour(hour).withMinute(0).withSecond(0).withNano(0).let {
            if (it.isAfter(now)) it else it.plusDays(1)
        }
        val request = PeriodicWorkRequestBuilder<EveningSyncWorker>(PERIOD_HOURS, TimeUnit.HOURS)
            .setInitialDelay(Duration.between(now, next).toMillis(), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(
            context,
        ).enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    companion object {
        const val WORK_NAME = "evening_briefing_sync"
        private const val PERIOD_HOURS = 24L
    }
}
