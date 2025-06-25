package dev.limebeck.openconf.domain.questions.service

import dev.limebeck.openconf.domain.Answer
import dev.limebeck.openconf.domain.questions.repository.Question
import dev.limebeck.openconf.domain.questions.repository.QuestionRepository
import io.ktor.server.plugins.*
import java.util.*

class QuestionServiceImpl(
    private val questionRepository: QuestionRepository
) : QuestionService {
    override suspend fun getQuestionsByBotId(botId: String): List<Question> {
        return questionRepository.findQuestionsByBotId(UUID.fromString(botId))
    }

    override suspend fun getQuestionById(id: String): Question? {
        return questionRepository.findQuestionById(UUID.fromString(id))
    }

    override suspend fun addQuestion(question: String, botId: String) {
        questionRepository.addQuestion(question, UUID.fromString(botId))
    }

    override suspend fun updateQuestion(id: String, question: String) {
        val existingQuestion = questionRepository.findQuestionById(UUID.fromString(id))
            ?: throw NotFoundException("Question not found")

        if (existingQuestion.question != question ) {
            questionRepository.updateQuestion(UUID.fromString(id), question)
        }
    }

    override suspend fun deleteQuestion(id: String) {
        val existingQuestion = questionRepository.findQuestionById(UUID.fromString(id))
            ?: throw NotFoundException("Question not found")

        questionRepository.deleteQuestion(UUID.fromString(id))
    }

    override suspend fun getQuestionAnswers(id: String, botId: String): List<Answer> {
        val existingQuestion = questionRepository.findQuestionById(UUID.fromString(id))
            ?: throw NotFoundException("Question not found")
        return questionRepository.findQuestionAnswers(UUID.fromString(id), UUID.fromString(botId))
    }

    override suspend fun getAllAnswersByBotId(botId: String): List<Answer> {
        return questionRepository.findAllAnswersByBotId(UUID.fromString(botId))
    }
}