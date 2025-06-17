package dev.limebeck.openconf.domain

import dev.inmo.tgbotapi.types.RawChatId
import dev.inmo.tgbotapi.types.UserId
import dev.limebeck.openconf.DbConfig
import dev.limebeck.openconf.QuestionId
import dev.limebeck.openconf.common.TimeProvider
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.schema.*
import java.util.*

object AnswersTable : Table<Nothing>("answers") {
    val id = uuid("id").primaryKey()
    val userId = long("user_id")
    val userName = varchar("username")
    val questionId = varchar("question_id")
    val answer = varchar("answer")
    val dateTime = timestamp("date_time")
}

object ActiveQuestions : Table<Nothing>("active_questions") {
    val userId = long("user_id").primaryKey()
    val questionId = varchar("question_id")
}

object UserStates : Table<Nothing>("user_state") {
    val userId = long("user_id").primaryKey()
    val username = varchar("username")
    val state = varchar("state")
    val updateTime = timestamp("update_time")
}

class QuestionsRepositoryKtorm(
    config: DbConfig.KtormConfig,
    private val timeProvider: TimeProvider
) : QuestionsRepository {
    private val database = Database.connect(
        url = config.url,
        driver = config.driver,
        user = config.username,
        password = config.password
    )

    private fun answersMapper(r: QueryRowSet) = Answer(
        questionId = QuestionId(r[AnswersTable.questionId]!!),
        userInfo = UserInfo(
            userId = UserId(RawChatId(r[AnswersTable.userId]!!)),
            username = r[AnswersTable.userName]!!
        ),
        answer = r[AnswersTable.answer]!!,
        dateTime = r[AnswersTable.dateTime]!!
    )

    override fun getUserAnswers(userId: UserId): List<Answer> {
        val answers = database
            .from(AnswersTable)
            .select()
            .where { AnswersTable.userId eq userId.chatId.long }
            .map(::answersMapper)
        return answers
    }

    override fun getAllAnswers(): List<Answer> {
        return database
            .from(AnswersTable)
            .select()
            .map(::answersMapper)
    }

    override fun addAnswer(userInfo: UserInfo, questionId: QuestionId, answer: String) {
        database.insert(AnswersTable) {
            set(AnswersTable.id, UUID.randomUUID())
            set(AnswersTable.questionId, questionId.value)
            set(AnswersTable.userId, userInfo.userId.chatId.long)
            set(AnswersTable.userName, userInfo.username)
            set(AnswersTable.answer, answer)
            set(AnswersTable.dateTime, timeProvider.getCurrent())
        }
    }

    override fun getActiveQuestion(userId: UserId): QuestionId? {
        return database.from(ActiveQuestions)
            .select(ActiveQuestions.questionId)
            .where { ActiveQuestions.userId eq userId.chatId.long }
            .map { it[ActiveQuestions.questionId] }
            .firstOrNull()?.let(::QuestionId)
    }

    override fun setActiveQuestion(userId: UserId, questionId: QuestionId?) {
        val exists = database.from(ActiveQuestions).select()
            .where { ActiveQuestions.userId eq userId.chatId.long }
            .map { it[ActiveQuestions.questionId] }.isNotEmpty()
        if (!exists) {
            database.insert(ActiveQuestions) {
                set(ActiveQuestions.questionId, questionId?.value)
                set(ActiveQuestions.userId, userId.chatId.long)
            }
        } else {
            database.update(ActiveQuestions) {
                set(ActiveQuestions.questionId, questionId?.value)
                where { ActiveQuestions.userId eq userId.chatId.long }
            }
        }
    }

    override fun getUserState(userId: UserId): UserState {
        return database.from(UserStates)
            .select()
            .where { UserStates.userId eq userId.chatId.long }
            .map { it[UserStates.state] }
            .firstOrNull()?.let { UserState.valueOf(it) } ?: UserState.NOT_MEMBER
    }

    override fun setUserState(userInfo: UserInfo, state: UserState) {
        val exists = database.from(UserStates).select()
            .where { UserStates.userId eq userInfo.userId.chatId.long }
            .map { it[UserStates.state] }.isNotEmpty()
        if (!exists) {
            database.insert(UserStates) {
                set(AnswersTable.userName, userInfo.username)
                set(UserStates.userId, userInfo.userId.chatId.long)
                set(UserStates.state, state.name)
                set(UserStates.updateTime, timeProvider.getCurrent())
            }
        } else {
            database.update(UserStates) {
                set(UserStates.state, state.name)
                set(UserStates.updateTime, timeProvider.getCurrent())
                where { UserStates.userId eq userInfo.userId.chatId.long }
            }
        }
    }

    override fun getAllUserStates(): List<UserInfoWithState> {
        return database.from(UserStates)
            .select()
            .map {
                UserInfoWithState(
                    UserInfo(
                        userId = UserId(RawChatId(it[UserStates.userId]!!)),
                        username = it[UserStates.username]!!
                    ),
                    state = UserState.valueOf(it[UserStates.state]!!),
                    updateTime = it[UserStates.updateTime]!!
                )
            }
    }
}