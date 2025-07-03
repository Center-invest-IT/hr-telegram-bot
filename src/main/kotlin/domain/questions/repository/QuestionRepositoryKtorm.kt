package dev.limebeck.openconf.domain.questions.repository

import dev.inmo.tgbotapi.types.RawChatId
import dev.inmo.tgbotapi.types.UserId
import dev.limebeck.openconf.BotId
import dev.limebeck.openconf.DbConfig
import dev.limebeck.openconf.QuestionId
import dev.limebeck.openconf.common.TimeProvider
import dev.limebeck.openconf.domain.Answer
import dev.limebeck.openconf.domain.AnswersTable
import dev.limebeck.openconf.domain.UserInfo
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.schema.Table
import org.ktorm.schema.timestamp
import org.ktorm.schema.uuid
import org.ktorm.schema.varchar
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
        id = QuestionId(r[QuestionsTable.id]!!.toString()),
        question = r[QuestionsTable.question]!!,
        botId = BotId(r[QuestionsTable.botId]!!.toString()),
        createdTime = r[QuestionsTable.createdTime]!!
    )

    override fun findQuestionsByBotId(botId: BotId): List<Question> {
        return database
            .from(QuestionsTable)
            .select()
            .where {
                QuestionsTable.botId eq UUID.fromString(botId.value)
            }
            .map(::questionsMapper)
    }

    override fun findQuestionById(questionId: QuestionId): Question? {
        return database
            .from(QuestionsTable)
            .select()
            .where {
                QuestionsTable.id eq UUID.fromString(questionId.value)
            }
            .map(::questionsMapper)
            .firstOrNull()
    }

    override fun addQuestion(question: String, botId: BotId): Question {
        val uuid = UUID.randomUUID()
        val currentTime = timeProvider.getCurrent()
        database.insert(QuestionsTable) {
            set(QuestionsTable.id, uuid)
            set(QuestionsTable.question, question)
            set(QuestionsTable.botId, UUID.fromString(botId.value))
            set(QuestionsTable.createdTime, currentTime)
        }

        return Question(
            QuestionId(uuid.toString()),
            question,
            botId,
            currentTime
        )
    }

    override fun updateQuestion(questionId: QuestionId, question: String) {
        database.update(QuestionsTable) {
            set(QuestionsTable.question, question)
            where {
                QuestionsTable.id eq UUID.fromString(questionId.value)
            }
        }
    }

    override fun deleteQuestion(questionId: QuestionId) {
        database.delete(QuestionsTable) { QuestionsTable.id eq UUID.fromString(questionId.value) }
    }

    override fun findQuestionAnswers(questionId: QuestionId, botId: BotId): List<Answer> {
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
                (AnswersTable.questionId eq UUID.fromString(questionId.value)) and (QuestionsTable.botId eq UUID.fromString(botId.value))
            }
            .map(::answersMapper)
    }

    override fun findAllAnswersByBotId(botId: BotId): List<Answer> {
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
                QuestionsTable.botId eq UUID.fromString(botId.value)
            }
            .map(::answersMapper)
    }
}
