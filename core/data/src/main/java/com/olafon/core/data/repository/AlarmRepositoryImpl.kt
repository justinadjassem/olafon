package com.olafon.core.data.repository

import com.olafon.core.data.db.dao.AlarmDao
import com.olafon.core.data.mapper.toDomain
import com.olafon.core.data.mapper.toEntity
import com.olafon.core.domain.model.Alarm
import com.olafon.core.domain.repository.AlarmRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class AlarmRepositoryImpl @Inject constructor(private val dao: AlarmDao) : AlarmRepository {
    override fun observeAll(): Flow<List<Alarm>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun get(id: Long): Alarm? = dao.get(id)?.toDomain()

    override suspend fun enabled(): List<Alarm> = dao.enabled().map { it.toDomain() }

    override suspend fun upsert(alarm: Alarm): Long {
        val rowId = dao.upsert(alarm.toEntity())
        return if (rowId == UPDATED_ROW) alarm.id else rowId
    }

    override suspend fun delete(id: Long) = dao.delete(id)

    private companion object {
        /** Room's `@Upsert` returns -1 when the row already existed and was updated. */
        const val UPDATED_ROW = -1L
    }
}
