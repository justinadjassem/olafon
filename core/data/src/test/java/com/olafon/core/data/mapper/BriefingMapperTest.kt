package com.olafon.core.data.mapper

import com.olafon.core.domain.briefing.Briefing
import com.olafon.core.domain.briefing.ScheduleItem
import com.olafon.core.domain.briefing.TaskItem
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class BriefingMapperTest {
    @Test
    fun `a briefing survives the round trip through its rows`() {
        val day = LocalDate.of(2026, 11, 17)
        val synced = Instant.parse("2026-11-16T22:00:00Z")
        val briefing = Briefing(
            day = day,
            events = listOf(
                ScheduleItem(
                    "device_calendar",
                    "12",
                    "Férié",
                    Instant.parse("2026-11-17T00:00:00Z"),
                    Instant.parse("2026-11-18T00:00:00Z"),
                    allDay = true,
                ),
                ScheduleItem(
                    "device_calendar",
                    "7",
                    "Revue de sprint",
                    Instant.parse("2026-11-17T08:00:00Z"),
                    Instant.parse("2026-11-17T09:00:00Z"),
                ),
            ),
            tasks = listOf(TaskItem("tasks", "a", "Impôts", overdue = true, priority = 1)),
            syncedAt = synced,
        )

        assertEquals(briefing, briefing.toEntities().toBriefing(day, synced))
    }
}
