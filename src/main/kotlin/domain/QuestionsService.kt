package dev.limebeck.openconf.domain

import dev.inmo.tgbotapi.types.UserId
import dev.limebeck.openconf.Question
import dev.limebeck.openconf.QuestionId

class QuestionsService(
    val questions: List<Question>,
    val questionsRepository: QuestionsRepository
) {
    suspend fun getById(id: QuestionId, userId: UserId): UserQuestion =
        getAll(userId).find { it.question.id == id }
            ?: throw RuntimeException("<a577542c> Вопрос c id $id не найден")

    suspend fun getActive(userId: UserId): Question? {
        return questionsRepository.getActiveQuestion(userId)?.let { questionId ->
            questions.find { it.id == questionId }
        }
    }

    suspend fun markActiveQuestion(userId: UserId, questionId: QuestionId?) {
        questionsRepository.setActiveQuestion(userId, questionId)
    }

    suspend fun getAll(userId: UserId): List<UserQuestion> {
        val reponses = questionsRepository.getUserAnswers(userId)
        return questions.map { q ->
            val response = reponses.find { it.questionId == q.id }
            UserQuestion(
                question = q,
                completed = response != null,
                response = response?.answer
            )
        }
    }

    suspend fun addAnswer(userInfo: UserInfo, questionId: QuestionId, response: String) {
        questionsRepository.addAnswer(userInfo, questionId, response)
    }

    suspend fun setUserState(userInfo: UserInfo, state: UserState) {
        questionsRepository.setUserState(userInfo, state)
    }

    suspend fun getUserState(userId: UserId): UserState {
        return questionsRepository.getUserState(userId)
    }
}

data class UserQuestion(
    val question: Question,
    val completed: Boolean,
    val response: String?
)