package dev.limebeck.openconf.domain

import dev.inmo.tgbotapi.types.RawChatId
import dev.inmo.tgbotapi.types.UserId
import dev.limebeck.openconf.common.html.RowDataProvider
import dev.limebeck.openconf.common.html.respondTable
import dev.limebeck.openconf.common.html.toTableCell
import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlinx.html.a

fun Routing.createQuestionRoutes(
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
                "State" to { +state.name }
            )
        }
    }
}