package com.olafon.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.olafon.core.data.db.entity.QuestionEntity

@Dao
interface QuestionDao {
    @Query("SELECT COUNT(*) FROM questions")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(questions: List<QuestionEntity>)

    @Query(
        """
        SELECT * FROM questions
        WHERE difficulty = :difficulty
          AND (:category IS NULL OR category = :category)
          AND id NOT IN (:excludeIds)
        ORDER BY RANDOM() LIMIT 1
        """,
    )
    suspend fun random(difficulty: String, category: String?, excludeIds: Set<String>): QuestionEntity?

    @Query(
        """
        SELECT * FROM questions
        WHERE (:category IS NULL OR category = :category)
          AND id NOT IN (:excludeIds)
        ORDER BY RANDOM() LIMIT 1
        """,
    )
    suspend fun randomAnyDifficulty(category: String?, excludeIds: Set<String>): QuestionEntity?
}
