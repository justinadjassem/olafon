package com.olafon.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.olafon.core.data.db.entity.WakeSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WakeSessionDao {
    @Insert
    suspend fun insert(session: WakeSessionEntity): Long

    @Query("SELECT * FROM wake_sessions WHERE id = :id")
    suspend fun get(id: Long): WakeSessionEntity?

    @Query("UPDATE wake_sessions SET attempts = attempts + 1 WHERE id = :id")
    suspend fun incrementAttempts(id: Long)

    @Query("UPDATE wake_sessions SET snoozes = snoozes + 1 WHERE id = :id")
    suspend fun incrementSnoozes(id: Long)

    @Query("UPDATE wake_sessions SET dismissedAt = :dismissedAt WHERE id = :id")
    suspend fun dismiss(id: Long, dismissedAt: Long)

    @Query("SELECT * FROM wake_sessions ORDER BY ringAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<WakeSessionEntity>>
}
