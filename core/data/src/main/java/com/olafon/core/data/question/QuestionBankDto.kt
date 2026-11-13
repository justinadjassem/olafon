package com.olafon.core.data.question

import kotlinx.serialization.Serializable

@Serializable
data class QuestionBankDto(val version: Int, val questions: List<QuestionDto>)

/** One entry of `assets/questions_fr.json`. Labels are French to keep the file easy to edit by hand. */
@Serializable
data class QuestionDto(
    val id: String,
    /** `culture`, `logique` or `vocabulaire`. */
    val categorie: String,
    /** `facile`, `moyen` or `difficile`. */
    val niveau: String,
    val question: String,
    val choix: List<String>,
    /** Index of the right answer in [choix]. */
    val reponse: Int,
)
