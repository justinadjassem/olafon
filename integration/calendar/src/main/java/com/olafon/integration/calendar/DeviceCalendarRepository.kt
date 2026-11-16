package com.olafon.integration.calendar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.olafon.core.domain.model.DeviceCalendar
import com.olafon.core.domain.repository.CalendarRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Lists the phone's calendars so the user can choose which ones feed the briefing. */
class DeviceCalendarRepository @Inject constructor(@ApplicationContext private val context: Context) :
    CalendarRepository {
    override fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) ==
            PackageManager.PERMISSION_GRANTED

    override suspend fun calendars(): List<DeviceCalendar> = withContext(Dispatchers.IO) {
        if (!hasPermission()) return@withContext emptyList()
        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            PROJECTION,
            "${CalendarContract.Calendars.VISIBLE} = 1",
            null,
            "${CalendarContract.Calendars.ACCOUNT_NAME}, ${CalendarContract.Calendars.CALENDAR_DISPLAY_NAME}",
        )?.use { cursor ->
            generateSequence { if (cursor.moveToNext()) cursor else null }
                .map { DeviceCalendar(it.getLong(0), it.getString(1).orEmpty(), it.getString(2).orEmpty()) }
                .toList()
        }.orEmpty()
    }

    private companion object {
        val PROJECTION = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME,
        )
    }
}
