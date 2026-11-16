package com.olafon.core.domain.briefing

import java.time.LocalDate

/**
 * Somewhere events and tasks come from: the phone's calendars today, task services later. Sources are bound into
 * a set, so adding one is a new module and nothing else.
 */
interface ScheduleSource {
    val id: String

    /** False when the source cannot be read right now (permission missing, account disconnected). */
    suspend fun isAvailable(): Boolean

    suspend fun fetchEvents(day: LocalDate): List<ScheduleItem>

    suspend fun fetchTasks(day: LocalDate): List<TaskItem>
}
