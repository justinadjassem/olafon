package com.olafon.core.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "wake_sessions",
    indices = [Index("alarmId"), Index("ringAt")],
)
data class WakeSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alarmId: Long,
    /** Epoch milliseconds. */
    val ringAt: Long,
    val dismissedAt: Long?,
    val attempts: Int,
    val snoozes: Int,
)
