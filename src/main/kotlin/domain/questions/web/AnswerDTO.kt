package dev.limebeck.openconf.domain.questions.web

import kotlinx.serialization.Serializable

@Serializable
data class AnswerDTO(
    val questionId: String,
    val userId: String,
    val username: String,
    val answer: String,
    val dateTime: String
)