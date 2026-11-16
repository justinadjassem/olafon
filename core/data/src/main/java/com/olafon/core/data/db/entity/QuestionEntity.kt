package com.olafon.core.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "questions",
    indices = [Index("category", "difficulty")],
)
data class QuestionEntity(
    @PrimaryKey val id: String,
    val category: String,
    val difficulty: String,
    val prompt: String,
    /** JSON array of strings. */
    val choices: String,
    val answerIndex: Int,
)
