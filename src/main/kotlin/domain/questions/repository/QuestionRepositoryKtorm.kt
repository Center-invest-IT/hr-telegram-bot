package dev.limebeck.openconf.domain.questions.repository

import dev.inmo.tgbotapi.types.RawChatId
import dev.inmo.tgbotapi.types.UserId
import dev.limebeck.openconf.DbConfig
import dev.limebeck.openconf.QuestionId
import dev.limebeck.openconf.common.TimeProvider
import dev.limebeck.openconf.domain.Answer
import dev.limebeck.openconf.domain.AnswersTable
import dev.limebeck.openconf.domain.UserInfo
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.schema.*
import java.util.*

object QuestionsTable : Table<Nothing>("questions") {
    val id = uuid("id").primaryKey()
    val question = varchar("question")
    val botId = uuid("bot_id")
    val createdTime = timestamp("created_time")
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

    private fun answersMapper(r: QueryRowSet) = Answer(
        questionId = QuestionId((r[AnswersTable.questionId]!!).toString()),
        userInfo = UserInfo(
            userId = UserId(RawChatId(r[AnswersTable.userId]!!)),
            username = r[AnswersTable.userName]!!
        ),
        answer = r[AnswersTable.answer]!!,
        dateTime = r[AnswersTable.dateTime]!!
    )

    private fun questionsMapper(r: QueryRowSet) = Question(
        id = r[QuestionsTable.id]!!,
        question = r[QuestionsTable.question]!!,
        botId = r[QuestionsTable.botId]!!,
        createdTime = r[QuestionsTable.createdTime]!!
    )

    override fun findQuestionsByBotId(botId: UUID): List<Question> {
        return database
            .from(QuestionsTable)
            .select()
            .where {
                QuestionsTable.botId eq botId
            }
            .map(::questionsMapper)
    }

    override fun findQuestionById(id: UUID): Question? {
        return database
            .from(QuestionsTable)
            .select()
            .where {
                QuestionsTable.id eq id
            }
            .map { row ->
                Question(
                    id = row[QuestionsTable.id]!!,
                    question = row[QuestionsTable.question]!!,
                    botId = row[QuestionsTable.botId]!!,
                    createdTime = row[QuestionsTable.createdTime]!!
                )
            }
            .firstOrNull()
    }

    override fun addQuestion(question: String, botId: UUID) {
        database.insert(QuestionsTable) {
            set(QuestionsTable.id, UUID.randomUUID())
            set(QuestionsTable.question, question)
            set(QuestionsTable.botId, botId)
            set(QuestionsTable.createdTime, timeProvider.getCurrent())
        }
    }

    override fun updateQuestion(id: UUID, question: String) {
        database.update(QuestionsTable) {
            set(QuestionsTable.question, question)
            where {
                QuestionsTable.id eq id
            }
        }
    }

    override fun deleteQuestion(id: UUID) {
        database.delete(QuestionsTable) { QuestionsTable.id eq id }
    }

    override fun findQuestionAnswers(id: UUID, botId: UUID): List<Answer> {
        return database
            .from(AnswersTable)
            .innerJoin(QuestionsTable, on = AnswersTable.questionId eq QuestionsTable.id)
            .select(
                AnswersTable.id,
                AnswersTable.userId,
                AnswersTable.userName,
                AnswersTable.questionId,
                AnswersTable.answer,
                AnswersTable.dateTime
            )
            .where {
                (AnswersTable.questionId eq id) and (QuestionsTable.botId eq botId)
            }
            .map(::answersMapper)
    }

    override fun findAllAnswersByBotId(botId: UUID): List<Answer> {
        return database
            .from(AnswersTable)
            .innerJoin(QuestionsTable, on = AnswersTable.questionId eq QuestionsTable.id)
            .select(
                AnswersTable.id,
                AnswersTable.userId,
                AnswersTable.userName,
                AnswersTable.questionId,
                AnswersTable.answer,
                AnswersTable.dateTime
            )
            .where {
                QuestionsTable.botId eq botId
            }
            .map(::answersMapper)
    }
}
