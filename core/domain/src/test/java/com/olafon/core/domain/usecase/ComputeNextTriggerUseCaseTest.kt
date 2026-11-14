package com.olafon.core.domain.usecase

import com.olafon.core.domain.model.Alarm
import java.time.Clock
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class ComputeNextTriggerUseCaseTest {
    private val paris: ZoneId = ZoneId.of("Europe/Paris")
    private val lome: ZoneId = ZoneId.of("Africa/Lome")
    private val useCase = ComputeNextTriggerUseCase(Clock.systemUTC())

    private fun at(text: String, zone: ZoneId): ZonedDateTime = ZonedDateTime.parse(text).withZoneSameInstant(zone)

    @Test
    fun `one-shot alarm later today rings today`() {
        val from = at("2026-11-13T06:00:00Z", lome) // vendredi 06:00
        val next = useCase(Alarm(hour = 7, minute = 30), from)
        assertEquals(at("2026-11-13T07:30:00Z", lome).toInstant(), next)
    }

    @Test
    fun `one-shot alarm already past rings tomorrow`() {
        val from = at("2026-11-13T08:00:00Z", lome)
        val next = useCase(Alarm(hour = 7, minute = 30), from)
        assertEquals(at("2026-11-14T07:30:00Z", lome).toInstant(), next)
    }

    @Test
    fun `alarm at the exact current minute is pushed to the next occurrence`() {
        val from = at("2026-11-13T07:30:00Z", lome)
        val next = useCase(Alarm(hour = 7, minute = 30), from)
        assertEquals(at("2026-11-14T07:30:00Z", lome).toInstant(), next)
    }

    @Test
    fun `weekday alarm on a friday evening skips to monday`() {
        val from = at("2026-11-13T20:00:00Z", lome) // vendredi
        val weekdays = setOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
        )
        val next = useCase(Alarm(hour = 6, minute = 45, days = weekdays), from)
        assertEquals(at("2026-11-16T06:45:00Z", lome).toInstant(), next)
    }

    @Test
    fun `repeating alarm on the same weekday wraps a full week when its time is past`() {
        val from = at("2026-11-13T09:00:00Z", lome) // vendredi 09:00
        val next = useCase(Alarm(hour = 8, minute = 0, days = setOf(DayOfWeek.FRIDAY)), from)
        assertEquals(at("2026-11-20T08:00:00Z", lome).toInstant(), next)
    }

    @Test
    fun `spring forward gap shifts the alarm by one hour`() {
        // 29 mars 2026 : à 02:00 CET les horloges passent à 03:00 CEST. 02:30 n'existe pas.
        val from = ZonedDateTime.of(2026, 3, 28, 22, 0, 0, 0, paris)
        val next = useCase(Alarm(hour = 2, minute = 30, days = setOf(DayOfWeek.SUNDAY)), from)
        assertEquals(ZonedDateTime.of(2026, 3, 29, 3, 30, 0, 0, paris).toInstant(), next)
    }

    @Test
    fun `fall back overlap uses the first occurrence`() {
        // 25 octobre 2026 : à 03:00 CEST les horloges reviennent à 02:00 CET. 02:30 existe deux fois.
        val from = ZonedDateTime.of(2026, 10, 24, 22, 0, 0, 0, paris)
        val next = useCase(Alarm(hour = 2, minute = 30, days = setOf(DayOfWeek.SUNDAY)), from)
        assertEquals(at("2026-10-25T00:30:00Z", paris).toInstant(), next) // 02:30 CEST = 00:30 UTC
    }

    @Test
    fun `alarm across a dst change keeps its local time`() {
        val from = ZonedDateTime.of(2026, 3, 28, 12, 0, 0, 0, paris) // samedi, encore en CET
        val next = useCase(Alarm(hour = 7, minute = 0, days = setOf(DayOfWeek.SUNDAY)), from)
        assertEquals(ZonedDateTime.of(2026, 3, 29, 7, 0, 0, 0, paris).toInstant(), next)
    }
}
