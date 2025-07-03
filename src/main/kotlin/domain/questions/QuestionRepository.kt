package dev.limebeck.openconf.domain.questions

import org.ktorm.entity.Entity
import java.time.Instant
import java.util.UUID

interface QuestionRepository {
    fun findAllQuestions(): List<QuestionInfo>
    fun findAllAnswers(): List<AnswerInfo>
    fun findUserAnswers(userId: Long): List<AnswerInfo>
    fun addQuestion(question: String)
    fun addAnswer(userInfo: UserInfo, questionId: UUID, answer: String)
    fun deleteQuestion(id: UUID)
    fun updateQuestion(id: UUID, question: String)
}

interface Answer: Entity<Answer> {
    companion object: Entity.Factory<Answer>()
    val id: UUID
    val userId: Long
    val username: String?
    val question: Question
    val answer: String
    val createdTime: Instant
}

interface Question: Entity<Question> {
    companion object: Entity.Factory<Question>()
    val id: UUID
    val question: String
    val createdTime: Instant
}

data class QuestionInfo(
    val id: UUID,
    val question: String,
    val createdTime: Instant
)

data class AnswerInfo(
    val id: UUID,
    val userId: Long,
    val username: String?,
    val question: UUID,
    val answer: String,
    val createdTime: Instant
)

data class UserInfo(
    val userId: Long,
    val username: String?
)


