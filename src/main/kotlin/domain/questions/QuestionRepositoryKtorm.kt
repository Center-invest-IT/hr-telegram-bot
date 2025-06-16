package dev.limebeck.openconf.domain.questions

import dev.limebeck.openconf.DbConfig
import dev.limebeck.openconf.common.TimeProvider
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.schema.*
import java.util.*

object AnswersTable : Table<Answer>("answers") {
    val id = uuid("id").primaryKey().bindTo { it.id }
    val userId = long("user_id").bindTo { it.userId }
    val username = varchar("username").bindTo { it.username }
    val questionId = uuid("question_id").references(QuestionsTable) { it.question }
    val answer = varchar("answer").bindTo { it.answer }
    val createdTime = timestamp("created_time").bindTo { it.createdTime }
}

object QuestionsTable : Table<Question>("questions") {
    val id = uuid("id").primaryKey().bindTo { it.id }
    val question = varchar("question").bindTo { it.question }
    val createdTime = timestamp("created_time").bindTo { it.createdTime }
}

class QuestionRepositoryKtorm(
    config: DbConfig.KtormConfig,
    private val timeProvider: TimeProvider
) : QuestionRepository {
    val database = Database.connect(
        url = config.url,
        driver = config.driver,
        user = config.username,
        password = config.password
    )

    private fun questionsMapper(r: QueryRowSet): QuestionInfo = QuestionInfo(
        id = r[QuestionsTable.id]!!,
        question = r[QuestionsTable.question]!!,
        createdTime = r[QuestionsTable.createdTime]!!
    )

    private fun answersMapper(r: QueryRowSet): AnswerInfo = AnswerInfo(
        id = r[AnswersTable.id]!!,
        userId = r[AnswersTable.userId]!!,
        username = r[AnswersTable.username],
        question = r[AnswersTable.questionId]!!,
        answer = r[AnswersTable.answer]!!,
        createdTime = r[AnswersTable.createdTime]!!
    )

    override fun getAllQuestions(): List<QuestionInfo> {
        return database
            .from(QuestionsTable)
            .select()
            .map(::questionsMapper)
    }

    override fun getAllAnswers(): List<AnswerInfo> {
        return database
            .from(AnswersTable)
            .select()
            .map(::answersMapper)
    }

    override fun getUserAnswers(userId: Long): List<AnswerInfo> {
        return database
            .from(AnswersTable)
            .select()
            .where {
                AnswersTable.userId eq userId
            }
            .map(::answersMapper)
    }

    override fun addQuestion(question: String) {
        database.insert(QuestionsTable) {
            set(QuestionsTable.id, UUID.randomUUID())
            set(QuestionsTable.question, question)
            set(QuestionsTable.createdTime, timeProvider.getCurrent())
        }
    }

    override fun deleteQuestion(id: UUID) {
        database.delete(QuestionsTable) { QuestionsTable.id eq id }
    }

    override fun updateQuestion(id: UUID, question: String) {
        database.update(QuestionsTable) {
            set(QuestionsTable.question, question)
            where {
                QuestionsTable.id eq id
            }
        }
    }

    override fun addAnswer(userInfo: UserInfo, questionId: UUID, answer: String) {
        database.insert(AnswersTable) {
            set(AnswersTable.id, UUID.randomUUID())
            set(AnswersTable.userId, userInfo.userId)
            set(AnswersTable.username, userInfo.username)
            set(AnswersTable.answer, answer)
            set(AnswersTable.questionId, questionId)
            set(AnswersTable.createdTime, timeProvider.getCurrent())
        }
    }
}
