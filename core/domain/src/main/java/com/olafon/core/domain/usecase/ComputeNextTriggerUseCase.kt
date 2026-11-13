package com.olafon.core.domain.usecase

import com.olafon.core.domain.model.Alarm
import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.ZonedDateTime
import javax.inject.Inject

/**
 * Computes when [Alarm] should ring next, strictly after `from`.
 *
 * A one-shot alarm rings at the next occurrence of its time (today or tomorrow). A repeating alarm rings on the next
 * selected weekday. Local times that fall into a daylight-saving gap are shifted forward by the gap, and times in an
 * overlap use the earlier offset, as `ZonedDateTime.of` does.
 */
class ComputeNextTriggerUseCase @Inject constructor(private val clock: Clock) {
    operator fun invoke(alarm: Alarm, from: ZonedDateTime = ZonedDateTime.now(clock)): Instant {
        val time = LocalTime.of(alarm.hour, alarm.minute)
        val zone = from.zone
        for (offset in 0L..DAYS_IN_WEEK) {
            val date = from.toLocalDate().plusDays(offset)
            if (alarm.repeats && date.dayOfWeek !in alarm.days) continue
            val candidate = ZonedDateTime.of(date, time, zone)
            if (candidate.isAfter(from)) return candidate.toInstant()
        }
        error("unreachable: a day within the next week always matches")
    }

    private companion object {
        const val DAYS_IN_WEEK = 7L
    }
}
