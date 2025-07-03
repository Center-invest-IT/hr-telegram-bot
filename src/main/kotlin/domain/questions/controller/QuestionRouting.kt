package dev.limebeck.openconf.domain.questions.controller

import dev.limebeck.openconf.BotId
import dev.limebeck.openconf.QuestionId
import dev.limebeck.openconf.domain.questions.service.QuestionService
import dev.limebeck.openconf.domain.questions.web.CreateQuestionDTO
import dev.limebeck.openconf.domain.questions.web.mapper.toDto
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.questionRouting(
    questionService: QuestionService
) {
    route("/bots/{botId}") {
        route("/questions") {
            get {
                val botIdString = call.parameters["botId"] ?: kotlin.run {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }

                try {
                    val botId = BotId(botIdString)
                    val questions = questionService.getQuestionsByBotId(botId).map { it.toDto() }
                    println("questions: $questions" )
                    call.respond(HttpStatusCode.OK, questions)
                } catch (e: Exception) {
                    println("exception: ${e.message}")
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            post {
                val botIdString = call.parameters["botId"] ?: kotlin.run {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }

                val createdQuestion = call.receiveNullable<CreateQuestionDTO>() ?: kotlin.run {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }

                try {
                    val botId = BotId(botIdString)
                    val newQuestion = questionService.addQuestion(
                        question = createdQuestion.question,
                        botId = botId
                    )

                    call.respond(HttpStatusCode.Created, newQuestion.toDto())
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            put("/{questionId}") {
                val newQuestion = call.receiveNullable<CreateQuestionDTO>() ?: kotlin.run {
                    call.respond(HttpStatusCode.BadRequest)
                    return@put
                }

                val questionIdString = call.parameters["questionId"] ?: kotlin.run {
                    call.respond(HttpStatusCode.BadRequest)
                    return@put
                }

                try {
                    val questionId = QuestionId(questionIdString)
                    questionService.updateQuestion(
                        questionId = questionId,
                        question = newQuestion.question
                    )
                    call.respond(HttpStatusCode.NoContent)
                } catch (e: NotFoundException) {
                    call.respond(HttpStatusCode.NotFound)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            delete("/{questionId}") {
                val questionIdString = call.parameters["questionId"] ?: kotlin.run {
                    call.respond(HttpStatusCode.BadRequest)
                    return@delete
                }

                try {
                    val questionId = QuestionId(questionIdString)
                    questionService.deleteQuestion(questionId)
                    call.respond(HttpStatusCode.NoContent)
                } catch (e: NotFoundException) {
                    call.respond(HttpStatusCode.NotFound)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            get("/{questionId}/answers") {
                val botIdString = call.parameters["botId"] ?: kotlin.run {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }

                val questionIdString = call.parameters["questionId"] ?: kotlin.run {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }

                try {
                    val questionId = QuestionId(questionIdString)
                    val botId = BotId(botIdString)
                    val answers = questionService.getQuestionAnswers(questionId, botId).map { it.toDto() }
                    call.respond(HttpStatusCode.OK, answers)
                } catch (e: NotFoundException) {
                    call.respond(HttpStatusCode.NotFound)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }


        }

        route("/answers") {
            get {
                val botIdString = call.parameters["botId"] ?: kotlin.run {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }

                try {
                    val botId = BotId(botIdString)
                    val answers = questionService.getAllAnswersByBotId(botId).map { it.toDto() }
                    call.respond(HttpStatusCode.OK, answers)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }
    }
}
