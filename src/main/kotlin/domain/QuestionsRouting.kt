package dev.limebeck.openconf.domain

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import dev.inmo.tgbotapi.types.RawChatId
import dev.inmo.tgbotapi.types.UserId
import dev.limebeck.openconf.common.DEFAULT_FORMATTER
import dev.limebeck.openconf.common.format
import dev.limebeck.openconf.common.html.RowDataProvider
import dev.limebeck.openconf.common.html.respondTable
import dev.limebeck.openconf.common.html.toTableCell
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.a

fun Route.createQuestionRoutes(
    questionsRepository: QuestionsRepository
) {
    val answerTableMapper: Answer.() -> RowDataProvider = {
        mapOf(
            "UserId" to {
                val id = userInfo.userId.chatId.long.toString()
                a(href = "/answers/${id}") { +id }
            },
            "UserName" to {
                if (userInfo.username.startsWith("tg://"))
                    a(href = userInfo.username) { +userInfo.username }
                else
                    +userInfo.username
            },
            "QuestionId" to { +questionId.value },
            "Answer" to { +answer },
            "DateTime" to { dateTime.toTableCell() }
        )
    }

    get("/answers") {
        call.respondTable(
            title = "Ответы",
            dataProvider = questionsRepository::getAllAnswers,
            viewMapper = answerTableMapper
        )
    }

    get("/answers/csv") {
        val answers = questionsRepository.getAllAnswers().map {
            mapOf(
                "UserId" to it.userInfo.userId.chatId.long.toString(),
                "UserName" to it.userInfo.username,
                "QuestionId" to it.questionId.value,
                "Answer" to it.answer,
                "DateTime" to it.dateTime.format(DEFAULT_FORMATTER)
            )
        }
        val csvFile = csvWriter().writeAllAsString(
            listOf(
                (answers.firstOrNull() ?: emptyMap()).keys.toList(),
                *(answers.map { it.values.toList() }).toTypedArray()
            )
        )
        call.respondBytes(csvFile.encodeToByteArray(), contentType = ContentType.Text.CSV)
    }

    get("/answers/{userId}") {
        val userId = call.parameters["userId"]!!.toLong()
        call.respondTable(
            title = "Ответы",
            dataProvider = { questionsRepository.getUserAnswers(UserId(RawChatId(userId))) },
            viewMapper = answerTableMapper
        )
    }

    get("/users") {
        call.respondTable("Пользователи", dataProvider = questionsRepository::getAllUserStates) {
            mapOf(
                "UserId" to { +userInfo.userId.chatId.long.toString() },
                "UserName" to {
                    if (userInfo.username.startsWith("tg://"))
                        a(href = userInfo.username) { +userInfo.username }
                    else
                        +userInfo.username
                },
                "State" to { +state.name },
                "UpdateTime" to { updateTime.toTableCell() }
            )
        }
    }
    get("/users/csv") {
        val answers = questionsRepository.getAllUserStates().map {
            mapOf(
                "UserId" to it.userInfo.userId.chatId.long.toString(),
                "UserName" to it.userInfo.username,
                "State" to it.state.name,
                "UpdateTime" to it.updateTime.format(DEFAULT_FORMATTER)
            )
        }

        val csvFile = csvWriter().writeAllAsString(
            listOf(
                (answers.firstOrNull() ?: emptyMap()).keys.toList(),
                *(answers.map { it.values.toList() }).toTypedArray()
            )
        )
        call.respondBytes(csvFile.encodeToByteArray(), contentType = ContentType.Text.CSV)
    }
}