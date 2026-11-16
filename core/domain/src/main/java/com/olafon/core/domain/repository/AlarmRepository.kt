package com.olafon.core.domain.repository

import com.olafon.core.domain.model.Alarm
import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    fun observeAll(): Flow<List<Alarm>>

    suspend fun get(id: Long): Alarm?

    suspend fun enabled(): List<Alarm>

    /** Inserts or updates [alarm] and returns its id. */
    suspend fun upsert(alarm: Alarm): Long

    suspend fun delete(id: Long)
}
