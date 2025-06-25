package dev.limebeck.openconf.domain.questions.service

import dev.limebeck.openconf.domain.Answer
import dev.limebeck.openconf.domain.questions.repository.Question
import java.util.*

interface QuestionService {
    suspend fun getQuestionsByBotId(botId: String): List<Question>
    suspend fun getQuestionById(id: String): Question?
    suspend fun addQuestion(question: String, botId: String)
    suspend fun updateQuestion(id: String, question: String)
    suspend fun deleteQuestion(id: String)
    suspend fun getQuestionAnswers(id: String, botId: String): List<Answer>
    suspend fun getAllAnswersByBotId(botId: String): List<Answer>
}