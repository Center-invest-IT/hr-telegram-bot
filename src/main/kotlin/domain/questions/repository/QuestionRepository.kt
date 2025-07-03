package dev.limebeck.openconf.domain.questions.repository

import dev.limebeck.openconf.BotId
import dev.limebeck.openconf.QuestionId
import dev.limebeck.openconf.domain.Answer
import java.time.Instant

interface QuestionRepository {
    fun findQuestionsByBotId(botId: BotId): List<Question>
    fun findQuestionById(questionId: QuestionId): Question?
    fun addQuestion(question: String, botId: BotId): Question
    fun updateQuestion(questionId: QuestionId, question: String)
    fun deleteQuestion(questionId: QuestionId)
    fun findQuestionAnswers(questionId: QuestionId, botId: BotId): List<Answer>
    fun findAllAnswersByBotId(botId: BotId): List<Answer>
}

data class Question(
    val id: QuestionId,
    val question: String,
    val botId: BotId,
    val createdTime: Instant,
)