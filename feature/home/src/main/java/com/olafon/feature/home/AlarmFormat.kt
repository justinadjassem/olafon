package com.olafon.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.olafon.core.domain.model.Alarm
import com.olafon.core.domain.model.ChallengeType
import com.olafon.core.domain.model.Difficulty
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

/** « 06:00 » */
fun Alarm.timeText(): String = "%02d:%02d".format(hour, minute)

private val WEEKDAYS =
    setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
private val WEEKEND = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

/** « Semaine · lun – ven », « Week-end · sam, dim », « Tous les jours », « Une fois » or the listed days. */
@Composable
fun Alarm.daysText(): String = when {
    days.isEmpty() -> stringResource(R.string.days_once)
    days.size == DayOfWeek.entries.size -> stringResource(R.string.days_every_day)
    days == WEEKDAYS -> stringResource(R.string.days_weekdays)
    days == WEEKEND -> stringResource(R.string.days_weekend)
    else -> days.sorted().joinToString(", ") { it.shortName() }
}

fun DayOfWeek.shortName(): String = getDisplayName(TextStyle.SHORT, Locale.FRENCH).trimEnd('.')

fun DayOfWeek.initial(): String = getDisplayName(TextStyle.NARROW, Locale.FRENCH).uppercase(Locale.FRENCH)

@Composable
fun ChallengeType.label(): String = stringResource(
    when (this) {
        ChallengeType.PATTERN -> R.string.challenge_pattern
        ChallengeType.QUESTION -> R.string.challenge_question
        ChallengeType.MATH -> R.string.challenge_math
    },
)

/** « Schéma », « Question », « Calcul »: fits a chip. */
@Composable
fun ChallengeType.shortLabel(): String = stringResource(
    when (this) {
        ChallengeType.PATTERN -> R.string.challenge_pattern_short
        ChallengeType.QUESTION -> R.string.challenge_question_short
        ChallengeType.MATH -> R.string.challenge_math_short
    },
)

@Composable
fun Difficulty.label(): String = stringResource(
    when (this) {
        Difficulty.EASY -> R.string.difficulty_easy
        Difficulty.MEDIUM -> R.string.difficulty_medium
        Difficulty.HARD -> R.string.difficulty_hard
    },
)
