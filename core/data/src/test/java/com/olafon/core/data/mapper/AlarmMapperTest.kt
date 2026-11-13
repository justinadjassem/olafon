package com.olafon.core.data.mapper

import com.olafon.core.domain.model.Alarm
import com.olafon.core.domain.model.ChallengeType
import com.olafon.core.domain.model.Difficulty
import java.time.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Test

class AlarmMapperTest {
    @Test
    fun `days mask uses bit 0 for monday and bit 6 for sunday`() {
        assertEquals(0b0000001, setOf(DayOfWeek.MONDAY).toDaysMask())
        assertEquals(0b1000000, setOf(DayOfWeek.SUNDAY).toDaysMask())
        assertEquals(0b0011111, DayOfWeek.entries.take(5).toSet().toDaysMask())
        assertEquals(0, emptySet<DayOfWeek>().toDaysMask())
    }

    @Test
    fun `days mask round trips`() {
        val days = setOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY)
        assertEquals(days, days.toDaysMask().toDaysSet())
    }

    @Test
    fun `alarm round trips through its entity`() {
        val alarm = Alarm(
            id = 7,
            hour = 6,
            minute = 15,
            days = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
            label = "Travail",
            enabled = false,
            challenges = listOf(ChallengeType.QUESTION, ChallengeType.MATH),
            difficulty = Difficulty.HARD,
            ringtoneUri = "content://media/internal/audio/media/12",
            progressiveVolume = false,
            maxSnoozes = 1,
            briefingEnabled = false,
        )
        assertEquals(alarm, alarm.toEntity().toDomain())
    }

    @Test
    fun `one-shot alarm keeps an empty challenge list and no days`() {
        val alarm = Alarm(hour = 8, minute = 0, challenges = emptyList())
        val restored = alarm.toEntity().toDomain()
        assertEquals(emptySet<DayOfWeek>(), restored.days)
        assertEquals(emptyList<ChallengeType>(), restored.challenges)
    }
}
