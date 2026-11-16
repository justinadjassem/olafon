package com.olafon.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.olafon.core.data.db.entity.BriefingItemEntity
import com.olafon.core.data.db.entity.BriefingSyncEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BriefingDao {
    @Query("SELECT * FROM briefing_items WHERE day = :day ORDER BY id")
    fun observeItems(day: Long): Flow<List<BriefingItemEntity>>

    @Query("SELECT * FROM briefing_syncs WHERE day = :day")
    fun observeSync(day: Long): Flow<BriefingSyncEntity?>

    @Query("DELETE FROM briefing_items WHERE day = :day")
    suspend fun deleteItems(day: Long)

    @Insert
    suspend fun insertItems(items: List<BriefingItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSync(sync: BriefingSyncEntity)

    @Query("DELETE FROM briefing_items WHERE day < :day")
    suspend fun deleteItemsBefore(day: Long)

    @Query("DELETE FROM briefing_syncs WHERE day < :day")
    suspend fun deleteSyncsBefore(day: Long)

    @Transaction
    suspend fun replaceDay(sync: BriefingSyncEntity, items: List<BriefingItemEntity>) {
        deleteItems(sync.day)
        insertItems(items)
        upsertSync(sync)
    }

    @Transaction
    suspend fun deleteBefore(day: Long) {
        deleteItemsBefore(day)
        deleteSyncsBefore(day)
    }
}
