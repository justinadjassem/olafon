package com.olafon.core.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** One cached line of a briefing: an agenda event or a task of [day]. */
@Entity(tableName = "briefing_items", indices = [Index("day")])
data class BriefingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Epoch day the item belongs to. */
    val day: Long,
    val source: String,
    /** [TYPE_EVENT] or [TYPE_TASK]. */
    val type: String,
    val externalId: String,
    val title: String,
    /** Epoch milliseconds; null for a task. */
    val startAt: Long?,
    val endAt: Long?,
    val allDay: Boolean,
    val done: Boolean,
    val overdue: Boolean,
    val priority: Int,
) {
    companion object {
        const val TYPE_EVENT = "EVENT"
        const val TYPE_TASK = "TASK"
    }
}
