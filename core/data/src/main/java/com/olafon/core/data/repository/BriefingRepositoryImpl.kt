package com.olafon.core.data.repository

import com.olafon.core.data.db.dao.BriefingDao
import com.olafon.core.data.db.entity.BriefingSyncEntity
import com.olafon.core.data.mapper.toBriefing
import com.olafon.core.data.mapper.toEntities
import com.olafon.core.domain.briefing.Briefing
import com.olafon.core.domain.repository.BriefingRepository
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Singleton
class BriefingRepositoryImpl @Inject constructor(private val dao: BriefingDao) : BriefingRepository {
    override suspend fun save(briefing: Briefing, syncedAt: Instant) =
        dao.replaceDay(BriefingSyncEntity(briefing.day.toEpochDay(), syncedAt.toEpochMilli()), briefing.toEntities())

    override fun observe(day: LocalDate): Flow<Briefing?> =
        combine(dao.observeSync(day.toEpochDay()), dao.observeItems(day.toEpochDay())) { sync, items ->
            sync?.let { items.toBriefing(day, Instant.ofEpochMilli(it.syncedAt)) }
        }

    override suspend fun deleteBefore(day: LocalDate) = dao.deleteBefore(day.toEpochDay())
}
