package com.olafon.core.domain.briefing

import com.olafon.core.domain.repository.BriefingRepository
import com.olafon.core.domain.usecase.SyncBriefingUseCase
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BriefingSyncTest {
    private val now: Instant = Instant.parse("2026-11-16T21:00:00Z")
    private val clock: Clock = Clock.fixed(now, ZoneOffset.UTC)
    private val today: LocalDate = LocalDate.of(2026, 11, 16)

    private class FakeSource(
        override val id: String,
        private val available: Boolean = true,
        private val events: List<ScheduleItem> = emptyList(),
        private val tasks: List<TaskItem> = emptyList(),
        private val fails: Boolean = false,
    ) : ScheduleSource {
        override suspend fun isAvailable() = available

        override suspend fun fetchEvents(day: LocalDate): List<ScheduleItem> {
            check(!fails) { "source down" }
            return events
        }

        override suspend fun fetchTasks(day: LocalDate) = tasks
    }

    private class FakeBriefingRepository : BriefingRepository {
        val days = MutableStateFlow(mapOf<LocalDate, Briefing>())

        override suspend fun save(briefing: Briefing, syncedAt: Instant) {
            days.value = days.value + (briefing.day to briefing.copy(syncedAt = syncedAt))
        }

        override fun observe(day: LocalDate): Flow<Briefing?> = days.map { it[day] }

        override suspend fun deleteBefore(day: LocalDate) {
            days.value = days.value.filterKeys { it >= day }
        }
    }

    private fun event(title: String, hour: Int, allDay: Boolean = false) = ScheduleItem(
        sourceId = "s",
        externalId = title,
        title = title,
        start = now.plusSeconds(hour * 3600L),
        end = now.plusSeconds(hour * 3600L + 1800),
        allDay = allDay,
    )

    @Test
    fun `merges sources, all-day events first then by start, overdue tasks first then by priority`() = runTest {
        val aggregator = BriefingAggregator(
            setOf(
                FakeSource("a", events = listOf(event("Sprint", 11), event("Férié", 0, allDay = true))),
                FakeSource(
                    "b",
                    events = listOf(event("Café", 9)),
                    tasks = listOf(
                        TaskItem("b", "1", "Courses", priority = 1),
                        TaskItem("b", "2", "Impôts", overdue = true),
                    ),
                ),
            ),
        )

        val briefing = aggregator.build(today)!!

        assertEquals(listOf("Férié", "Café", "Sprint"), briefing.events.map { it.title })
        assertEquals(listOf("Impôts", "Courses"), briefing.tasks.map { it.title })
        assertEquals("Café", briefing.firstTimedEvent?.title)
        assertEquals(1, briefing.overdueTasks)
    }

    @Test
    fun `a failing source never breaks the others, and no available source gives nothing`() = runTest {
        val partial =
            BriefingAggregator(
                setOf(FakeSource("down", fails = true), FakeSource("ok", events = listOf(event("Café", 9)))),
            )
        assertEquals(listOf("Café"), partial.build(today)!!.events.map { it.title })

        assertNull(BriefingAggregator(setOf(FakeSource("off", available = false))).build(today))
        assertNull(BriefingAggregator(emptySet()).build(today))
    }

    @Test
    fun `sync caches today and tomorrow, stamps them and drops older days`() = runTest {
        val repository = FakeBriefingRepository()
        repository.save(Briefing(today.minusDays(1)), now)
        val sync =
            SyncBriefingUseCase(
                BriefingAggregator(setOf(FakeSource("a", events = listOf(event("Café", 9))))),
                repository,
                clock,
            )

        assertTrue(sync())

        assertEquals(setOf(today, today.plusDays(1)), repository.days.value.keys)
        assertEquals(now, repository.days.value.getValue(today).syncedAt)
    }

    @Test
    fun `sync leaves the cache untouched when no source can be read`() = runTest {
        val repository = FakeBriefingRepository()
        repository.save(Briefing(today, events = listOf(event("Café", 9))), now)
        val sync =
            SyncBriefingUseCase(BriefingAggregator(setOf(FakeSource("off", available = false))), repository, clock)

        assertFalse(sync())

        assertEquals(1, repository.days.value.getValue(today).events.size)
    }
}
