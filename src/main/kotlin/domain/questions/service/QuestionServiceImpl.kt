package dev.limebeck.openconf.domain.questions.service

import dev.limebeck.openconf.BotId
import dev.limebeck.openconf.QuestionId
import dev.limebeck.openconf.domain.Answer
import dev.limebeck.openconf.domain.questions.repository.Question
import dev.limebeck.openconf.domain.questions.repository.QuestionRepository
import io.ktor.server.plugins.*

class QuestionServiceImpl(
    private val questionRepository: QuestionRepository
) : QuestionService {
    override suspend fun getQuestionsByBotId(botId: BotId): List<Question> {
        return questionRepository.findQuestionsByBotId(botId)
    }

    override suspend fun getQuestionById(questionId: QuestionId): Question? {
        return questionRepository.findQuestionById(questionId)
    }

    override suspend fun addQuestion(question: String, botId: BotId): Question {
        return questionRepository.addQuestion(question, botId)
    }

    override suspend fun updateQuestion(questionId: QuestionId, question: String) {
        val existingQuestion = questionRepository.findQuestionById(questionId)
            ?: throw NotFoundException("Question not found")

        if (existingQuestion.question != question ) {
            questionRepository.updateQuestion(questionId, question)
        }
    }

    override suspend fun deleteQuestion(questionId: QuestionId) {
        val existingQuestion = questionRepository.findQuestionById(questionId)
            ?: throw NotFoundException("Question not found")

        questionRepository.deleteQuestion(questionId)
    }

    override suspend fun getQuestionAnswers(questionId: QuestionId, botId: BotId): List<Answer> {
        val existingQuestion = questionRepository.findQuestionById(questionId)
            ?: throw NotFoundException("Question not found")
        return questionRepository.findQuestionAnswers(questionId, botId)
    }

    override suspend fun getAllAnswersByBotId(botId: BotId): List<Answer> {
        return questionRepository.findAllAnswersByBotId(botId)
    }
}