package com.olafon.core.domain.repository

import com.olafon.core.domain.model.RingInterruption
import com.olafon.core.domain.model.WakeSession
import java.time.Instant
import kotlinx.coroutines.flow.Flow

interface WakeSessionRepository {
    /** Records that [alarmId] started ringing at [ringAt] and returns the session id. */
    suspend fun start(alarmId: Long, ringAt: Instant): Long

    suspend fun recordAttempt(sessionId: Long)

    suspend fun recordSnooze(sessionId: Long)

    /** Closes the session. [emergency] marks a ring stopped with the emergency exit code. */
    suspend fun dismiss(sessionId: Long, at: Instant, emergency: Boolean = false)

    /** Marks the ring of [sessionId] as cut short; a later record overrides an earlier one. */
    suspend fun recordInterruption(sessionId: Long, interruption: RingInterruption)

    suspend fun get(sessionId: Long): WakeSession?

    /** The most recent session not dismissed yet that started at or after [since], if any. */
    suspend fun latestOpenSince(since: Instant): WakeSession?

    fun observeRecent(limit: Int): Flow<List<WakeSession>>
}
