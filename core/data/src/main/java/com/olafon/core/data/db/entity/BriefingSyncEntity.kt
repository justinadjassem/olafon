package com.olafon.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Marks a day as synchronised, so an empty day is told apart from a day never read. */
@Entity(tableName = "briefing_syncs")
data class BriefingSyncEntity(
    /** Epoch day. */
    @PrimaryKey val day: Long,
    /** Epoch milliseconds. */
    val syncedAt: Long,
)
