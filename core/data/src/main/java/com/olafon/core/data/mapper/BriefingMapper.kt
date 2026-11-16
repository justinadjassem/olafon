package com.olafon.core.data.mapper

import com.olafon.core.data.db.entity.BriefingItemEntity
import com.olafon.core.domain.briefing.Briefing
import com.olafon.core.domain.briefing.ScheduleItem
import com.olafon.core.domain.briefing.TaskItem
import java.time.Instant
import java.time.LocalDate

/** Flattens a briefing into rows, events first, keeping the order the aggregator chose. */
fun Briefing.toEntities(): List<BriefingItemEntity> {
    val epochDay = day.toEpochDay()
    return events.map {
        BriefingItemEntity(
            day = epochDay,
            source = it.sourceId,
            type = BriefingItemEntity.TYPE_EVENT,
            externalId = it.externalId,
            title = it.title,
            startAt = it.start.toEpochMilli(),
            endAt = it.end.toEpochMilli(),
            allDay = it.allDay,
            done = false,
            overdue = false,
            priority = TaskItem.DEFAULT_PRIORITY,
        )
    } + tasks.map {
        BriefingItemEntity(
            day = epochDay,
            source = it.sourceId,
            type = BriefingItemEntity.TYPE_TASK,
            externalId = it.externalId,
            title = it.title,
            startAt = null,
            endAt = null,
            allDay = false,
            done = it.done,
            overdue = it.overdue,
            priority = it.priority,
        )
    }
}

fun List<BriefingItemEntity>.toBriefing(day: LocalDate, syncedAt: Instant): Briefing = Briefing(
    day = day,
    events = filter { it.type == BriefingItemEntity.TYPE_EVENT }.map {
        ScheduleItem(
            sourceId = it.source,
            externalId = it.externalId,
            title = it.title,
            start = Instant.ofEpochMilli(it.startAt ?: 0),
            end = Instant.ofEpochMilli(it.endAt ?: 0),
            allDay = it.allDay,
        )
    },
    tasks = filter { it.type == BriefingItemEntity.TYPE_TASK }.map {
        TaskItem(it.source, it.externalId, it.title, overdue = it.overdue, priority = it.priority, done = it.done)
    },
    syncedAt = syncedAt,
)
