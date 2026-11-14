package com.olafon.core.data.repository

import com.olafon.core.data.db.dao.WakeSessionDao
import com.olafon.core.data.db.entity.WakeSessionEntity
import com.olafon.core.data.mapper.toDomain
import com.olafon.core.domain.model.WakeSession
import com.olafon.core.domain.repository.WakeSessionRepository
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class WakeSessionRepositoryImpl @Inject constructor(private val dao: WakeSessionDao) : WakeSessionRepository {
    override suspend fun start(alarmId: Long, ringAt: Instant): Long = dao.insert(
        WakeSessionEntity(
            alarmId = alarmId,
            ringAt = ringAt.toEpochMilli(),
            dismissedAt = null,
            attempts = 0,
            snoozes = 0,
        ),
    )

    override suspend fun recordAttempt(sessionId: Long) = dao.incrementAttempts(sessionId)

    override suspend fun recordSnooze(sessionId: Long) = dao.incrementSnoozes(sessionId)

    override suspend fun dismiss(sessionId: Long, at: Instant, emergency: Boolean) =
        dao.dismiss(sessionId, at.toEpochMilli(), emergency)

    override suspend fun get(sessionId: Long): WakeSession? = dao.get(sessionId)?.toDomain()

    override fun observeRecent(limit: Int): Flow<List<WakeSession>> =
        dao.observeRecent(limit).map { list -> list.map(WakeSessionEntity::toDomain) }
}
