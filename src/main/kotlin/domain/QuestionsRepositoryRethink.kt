package dev.limebeck.openconf.domain

import com.rethinkdb.RethinkDB.r
import com.rethinkdb.net.Connection
import dev.inmo.tgbotapi.types.UserId
import dev.limebeck.openconf.DbConfig
import dev.limebeck.openconf.QuestionId
import dev.limebeck.openconf.common.TimeProvider

fun Connection.isTableExists(dbName: String, name: String): Boolean {
    return r.db(dbName).tableList().contains(name).run(this).first() as Boolean
}

class QuestionsRepositoryRethink(
    val dbConfig: DbConfig.RethinkDbConfig,
    val timeProvider: TimeProvider
) : QuestionsRepository {
    val connection = r.connection()
        .hostname(dbConfig.hostname)
        .port(dbConfig.port)
        .user(dbConfig.username, dbConfig.password)
        .connect()


    init {
        if (!connection.isTableExists(dbConfig.dbName, "answers")) {
            r.db(dbConfig.dbName).tableCreate("answers").run(connection)
        }
        if (!connection.isTableExists(dbConfig.dbName, "activeQuestion")) {
            r.db(dbConfig.dbName).tableCreate("activeQuestion").run(connection)
        }
    }

    override fun getUserAnswers(userId: UserId): List<Answer> {
        val userAnswersResult = r.table("answers")
            .filter { r -> r.g("userId").eq(userId.chatId) }
            .run(connection)

        userAnswersResult.map {        }
        return emptyList()
    }

    override fun getAllAnswers(): List<Answer> {
        TODO("Not yet implemented")
    }

    override fun addAnswer(userInfo: UserInfo, questionId: QuestionId, answer: String) {
        r.table("answers").insert(
            r.hashMap("userId", userInfo.userId.chatId)
                .with("questionId", questionId.value)
                .with("answer", answer)
        ).run(connection)
    }

    override fun getActiveQuestion(userId: UserId): QuestionId? {
        val questions = r.table("activeQuestion")
            .filter { it.g("userId").eq(userId.chatId) }.run(connection).map {
            it as Map<String, Any>
        }
        return TODO()
    }

    override fun setActiveQuestion(
        userId: UserId,
        questionId: QuestionId?
    ) {
        println(r.table("activeQuestion").contains("userId = ${userId.chatId.long}").run(connection).first())
        if (r.table("activeQuestion").contains("userId = ${userId.chatId.long}").run(connection).first() as Boolean) {
            r.table("activeQuestion").filter("userId = ${userId.chatId.long}").update(mapOf("questionId" to null)).run(connection)
        } else {
            r.table("activeQuestion").insert(
                r.hashMap("userId", userId.chatId.long)
                    .with("questionId", questionId?.value)
            ).run(connection)
        }
    }

    override fun getUserState(userId: UserId): UserState {
        TODO("Not yet implemented")
    }

    override fun setUserState(userInfo: UserInfo, state: UserState) {
        TODO("Not yet implemented")
    }

    override fun getAllUserStates(): List<UserInfoWithState> {
        TODO("Not yet implemented")
    }
}