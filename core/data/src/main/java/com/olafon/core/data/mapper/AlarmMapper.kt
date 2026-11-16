package com.olafon.core.data.mapper

import com.olafon.core.data.db.entity.AlarmEntity
import com.olafon.core.domain.model.Alarm
import com.olafon.core.domain.model.ChallengeType
import com.olafon.core.domain.model.Difficulty
import java.time.DayOfWeek

private const val CHALLENGE_SEPARATOR = ","

/** Bit 0 = Monday … bit 6 = Sunday, matching `DayOfWeek.value - 1`. */
fun Set<DayOfWeek>.toDaysMask(): Int = fold(0) { mask, day -> mask or (1 shl (day.value - 1)) }

fun Int.toDaysSet(): Set<DayOfWeek> {
    val mask = this
    return DayOfWeek.entries.filterTo(mutableSetOf()) { day -> mask and (1 shl (day.value - 1)) != 0 }
}

fun Alarm.toEntity(): AlarmEntity = AlarmEntity(
    id = id,
    hour = hour,
    minute = minute,
    daysMask = days.toDaysMask(),
    label = label,
    enabled = enabled,
    challenges = challenges.joinToString(CHALLENGE_SEPARATOR) { it.name },
    difficulty = difficulty.name,
    ringtoneUri = ringtoneUri,
    progressiveVolume = progressiveVolume,
    maxSnoozes = maxSnoozes,
    briefingEnabled = briefingEnabled,
)

fun AlarmEntity.toDomain(): Alarm = Alarm(
    id = id,
    hour = hour,
    minute = minute,
    days = daysMask.toDaysSet(),
    label = label,
    enabled = enabled,
    challenges = challenges.split(CHALLENGE_SEPARATOR).filter { it.isNotBlank() }.map { ChallengeType.valueOf(it) },
    difficulty = Difficulty.valueOf(difficulty),
    ringtoneUri = ringtoneUri,
    progressiveVolume = progressiveVolume,
    maxSnoozes = maxSnoozes,
    briefingEnabled = briefingEnabled,
)
