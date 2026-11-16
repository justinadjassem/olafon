package com.olafon.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hour: Int,
    val minute: Int,
    /** Bit 0 = Monday … bit 6 = Sunday. 0 = one-shot alarm. */
    val daysMask: Int,
    val label: String,
    val enabled: Boolean,
    /** Challenge type names in order, comma-separated, e.g. `PATTERN,QUESTION`. */
    val challenges: String,
    val difficulty: String,
    val ringtoneUri: String?,
    val progressiveVolume: Boolean,
    val maxSnoozes: Int,
    val briefingEnabled: Boolean,
)
