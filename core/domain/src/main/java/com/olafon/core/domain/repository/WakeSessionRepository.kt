package com.olafon.core.domain.repository

import com.olafon.core.domain.model.WakeSession
import java.time.Instant
import kotlinx.coroutines.flow.Flow

interface WakeSessionRepository {
    /** Records that [alarmId] started ringing at [ringAt] and returns the session id. */
    suspend fun start(alarmId: Long, ringAt: Instant): Long

    suspend fun recordAttempt(sessionId: Long)

    suspend fun recordSnooze(sessionId: Long)

    suspend fun dismiss(sessionId: Long, at: Instant)

    suspend fun get(sessionId: Long): WakeSession?

    fun observeRecent(limit: Int): Flow<List<WakeSession>>
}
