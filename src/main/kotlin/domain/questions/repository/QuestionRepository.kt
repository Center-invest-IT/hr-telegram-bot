package dev.limebeck.openconf.domain.questions.repository

import dev.limebeck.openconf.domain.Answer
import java.time.Instant
import java.util.UUID

interface QuestionRepository {
    fun findQuestionsByBotId(botId: UUID): List<Question>
    fun findQuestionById(id: UUID): Question?
    fun addQuestion(question: String, botId: UUID)
    fun updateQuestion(id: UUID, question: String)
    fun deleteQuestion(id: UUID)
    fun findQuestionAnswers(id: UUID, botId: UUID): List<Answer>
    fun findAllAnswersByBotId(botId: UUID): List<Answer>
}

data class Question(
    val id: UUID,
    val question: String,
    val botId: UUID,
    val createdTime: Instant,
)