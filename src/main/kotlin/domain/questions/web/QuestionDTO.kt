package dev.limebeck.openconf.domain.questions.web

import kotlinx.serialization.Serializable

@Serializable
data class CreateQuestionDTO(
    val question: String
)

@Serializable
data class QuestionDTO(
    val id: String,
    val question: String,
    val botId: String,
    val createdTime: String,
)