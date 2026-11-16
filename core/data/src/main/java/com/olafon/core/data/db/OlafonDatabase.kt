package com.olafon.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.olafon.core.data.db.dao.AlarmDao
import com.olafon.core.data.db.dao.BriefingDao
import com.olafon.core.data.db.dao.QuestionDao
import com.olafon.core.data.db.dao.WakeSessionDao
import com.olafon.core.data.db.entity.AlarmEntity
import com.olafon.core.data.db.entity.BriefingItemEntity
import com.olafon.core.data.db.entity.BriefingSyncEntity
import com.olafon.core.data.db.entity.QuestionEntity
import com.olafon.core.data.db.entity.WakeSessionEntity

@Database(
    entities = [
        AlarmEntity::class,
        QuestionEntity::class,
        WakeSessionEntity::class,
        BriefingItemEntity::class,
        BriefingSyncEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class OlafonDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao

    abstract fun questionDao(): QuestionDao

    abstract fun wakeSessionDao(): WakeSessionDao

    abstract fun briefingDao(): BriefingDao

    companion object {
        const val NAME = "olafon.db"
    }
}
