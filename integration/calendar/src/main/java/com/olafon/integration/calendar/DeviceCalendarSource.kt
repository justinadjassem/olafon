package com.olafon.integration.calendar

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.provider.CalendarContract
import com.olafon.core.domain.briefing.ScheduleItem
import com.olafon.core.domain.briefing.ScheduleSource
import com.olafon.core.domain.briefing.TaskItem
import com.olafon.core.domain.repository.CalendarRepository
import com.olafon.core.domain.repository.PreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Reads the day's events from every calendar synchronised on the phone. `Instances` already expands recurring
 * events. Calendars the user excluded in the settings are skipped.
 */
class DeviceCalendarSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val calendars: CalendarRepository,
    private val preferences: PreferencesRepository,
) : ScheduleSource {
    override val id: String = SOURCE_ID

    override suspend fun isAvailable(): Boolean = calendars.hasPermission()

    override suspend fun fetchEvents(day: LocalDate): List<ScheduleItem> = withContext(Dispatchers.IO) {
        val excluded = preferences.observe().first().excludedCalendarIds
        val zone = ZoneId.systemDefault()
        val start = day.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = day.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon()
            .also {
                ContentUris.appendId(it, start)
                ContentUris.appendId(it, end)
            }
            .build()
        context.contentResolver.query(uri, PROJECTION, null, null, "${CalendarContract.Instances.BEGIN} ASC")
            ?.use { cursor ->
                generateSequence { if (cursor.moveToNext()) cursor else null }
                    .filter { it.getLong(COL_CALENDAR_ID) !in excluded }
                    .map { it.toScheduleItem() }
                    // An all-day event is stored in UTC: one that ends at this day's midnight belongs to yesterday.
                    .filter { !it.allDay || it.start.atZone(ZoneOffset.UTC).toLocalDate() <= day }
                    .filter { !it.allDay || it.end.atZone(ZoneOffset.UTC).toLocalDate() > day }
                    .toList()
            }
            .orEmpty()
    }

    override suspend fun fetchTasks(day: LocalDate): List<TaskItem> = emptyList()

    private fun Cursor.toScheduleItem(): ScheduleItem = ScheduleItem(
        sourceId = SOURCE_ID,
        externalId = "${getLong(COL_EVENT_ID)}@${getLong(COL_BEGIN)}",
        title = getString(COL_TITLE).orEmpty(),
        start = Instant.ofEpochMilli(getLong(COL_BEGIN)),
        end = Instant.ofEpochMilli(getLong(COL_END)),
        allDay = getInt(COL_ALL_DAY) == 1,
    )

    companion object {
        const val SOURCE_ID = "device_calendar"

        private val PROJECTION = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY,
            CalendarContract.Instances.CALENDAR_ID,
        )
        private const val COL_EVENT_ID = 0
        private const val COL_TITLE = 1
        private const val COL_BEGIN = 2
        private const val COL_END = 3
        private const val COL_ALL_DAY = 4
        private const val COL_CALENDAR_ID = 5
    }
}
