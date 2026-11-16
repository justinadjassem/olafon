package com.olafon.core.data.db.entity

import androidx.room.ColumnInfo
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
    @ColumnInfo(defaultValue = "0") val emergencyExit: Boolean = false,
    /** Epoch milliseconds; set when the ring was cut short by a power-off, a reboot or a crash. */
    val interruptedAt: Long? = null,
    @ColumnInfo(defaultValue = "0") val cleanShutdown: Boolean = false,
    val interruptionBattery: Int? = null,
)
