package com.olafon.core.domain.briefing

import java.time.Instant
import java.time.LocalDate

/** What a day holds, as cached the evening before: agenda events and tasks, from every connected source. */
data class Briefing(
    val day: LocalDate,
    val events: List<ScheduleItem> = emptyList(),
    val tasks: List<TaskItem> = emptyList(),
    /** When the cache for [day] was last filled. */
    val syncedAt: Instant? = null,
) {
    val firstTimedEvent: ScheduleItem? get() = events.firstOrNull { !it.allDay }
    val overdueTasks: Int get() = tasks.count { it.overdue }
}

/** One agenda event occurrence. */
data class ScheduleItem(
    val sourceId: String,
    val externalId: String,
    val title: String,
    val start: Instant,
    val end: Instant,
    val allDay: Boolean = false,
)

/** One task due on the day, or overdue. A lower [priority] comes first. */
data class TaskItem(
    val sourceId: String,
    val externalId: String,
    val title: String,
    val overdue: Boolean = false,
    val priority: Int = DEFAULT_PRIORITY,
    val done: Boolean = false,
) {
    companion object {
        const val DEFAULT_PRIORITY = 3
    }
}
