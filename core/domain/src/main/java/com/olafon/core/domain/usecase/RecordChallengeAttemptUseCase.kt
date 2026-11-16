package com.olafon.core.domain.usecase

import com.olafon.core.domain.repository.WakeSessionRepository
import javax.inject.Inject

/** Counts every answer given during a ring, right or wrong, on its wake session. */
class RecordChallengeAttemptUseCase @Inject constructor(private val sessions: WakeSessionRepository) {
    suspend operator fun invoke(sessionId: Long) = sessions.recordAttempt(sessionId)
}
