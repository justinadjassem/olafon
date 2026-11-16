package com.olafon.core.domain.repository

import com.olafon.core.domain.model.DeviceCalendar

/** The calendars stored on the phone (Google, Outlook, Samsung…), read locally. */
interface CalendarRepository {
    fun hasPermission(): Boolean

    /** Empty when the permission is missing. */
    suspend fun calendars(): List<DeviceCalendar>
}
