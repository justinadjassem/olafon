package com.olafon.core.data.mapper

import com.olafon.core.data.db.entity.WakeSessionEntity
import com.olafon.core.domain.model.WakeSession
import java.time.Instant

fun WakeSessionEntity.toDomain(): WakeSession = WakeSession(
    id = id,
    alarmId = alarmId,
    ringAt = Instant.ofEpochMilli(ringAt),
    dismissedAt = dismissedAt?.let(Instant::ofEpochMilli),
    attempts = attempts,
    snoozes = snoozes,
    emergencyExit = emergencyExit,
)
