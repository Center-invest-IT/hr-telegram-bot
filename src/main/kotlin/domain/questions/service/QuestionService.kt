package dev.limebeck.openconf.domain.questions.service

import dev.limebeck.openconf.BotId
import dev.limebeck.openconf.QuestionId
import dev.limebeck.openconf.domain.Answer
import dev.limebeck.openconf.domain.questions.repository.Question

interface QuestionService {
    suspend fun getQuestionsByBotId(botId: BotId): List<Question>
    suspend fun getQuestionById(questionId: QuestionId): Question?
    suspend fun addQuestion(question: String, botId: BotId): Question
    suspend fun updateQuestion(questionId: QuestionId, question: String)
    suspend fun deleteQuestion(questionId: QuestionId)
    suspend fun getQuestionAnswers(questionId: QuestionId, botId: BotId): List<Answer>
    suspend fun getAllAnswersByBotId(botId: BotId): List<Answer>
}