package dev.limebeck.openconf.domain.bots.controller

import dev.limebeck.openconf.domain.bots.repository.BotFilter
import dev.limebeck.openconf.domain.bots.service.BotsService
import dev.limebeck.openconf.domain.bots.web.APIResponse
import dev.limebeck.openconf.domain.bots.web.BotStatusDTO
import dev.limebeck.openconf.domain.bots.web.CreateBotDTO
import dev.limebeck.openconf.domain.bots.web.mapper.toDTO
import dev.limebeck.openconf.domain.bots.web.mapper.toEntity
import dev.limebeck.openconf.domain.bots.web.util.Errors
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.botsRouting(
    botsService: BotsService
) {
    route("/bots") {
        get {
            try {
                val bots = botsService.getAllBots().map { it.toDTO() }
                call.respond(
                    HttpStatusCode.OK,
                    APIResponse("Bots found successfully", bots)
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.Conflict,
                    APIResponse(Errors.GENERAL, e.message)
                )
            }
        }

        get("/{id}") {
            val id = call.parameters["id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, APIResponse(Errors.MISS_ID, null))
                return@get
            }

            try {
                val bot = botsService.getBotById(id)
                if (bot == null) {
                    call.respond(HttpStatusCode.NotFound, APIResponse(Errors.BOT_NOT_FOUND, null))
                } else {
                    call.respond(HttpStatusCode.OK, APIResponse("Bot found successfully", bot.toDTO()))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, APIResponse(Errors.GENERAL, e.message))
            }
        }

        get("/search/{username}") {
            val username = call.parameters["username"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, APIResponse("Username is required", null))
                return@get
            }

            val statusParam = call.request.queryParameters["status"]

            val statusDTO = statusParam?.let {
                try {
                    BotStatusDTO.valueOf(it.uppercase())
                } catch (e: IllegalArgumentException) {
                    null
                }
            }

            val status = statusDTO?.toEntity()
            val filter = BotFilter(status = status)

            try {
                val bots = botsService.getBotByUsername(username, filter).map { it.toDTO() }
                call.respond(HttpStatusCode.OK, APIResponse("Bots found successfully", bots))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, APIResponse(Errors.GENERAL, e.message))
            }
        }

        post {
            val newBot = call.receiveNullable<CreateBotDTO>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, APIResponse(Errors.INVALID_BODY, null))
                return@post
            }

            try {
                botsService.addBot(
                    botUsername = newBot.botUsername,
                    botToken = newBot.botToken,
                    botDescription = newBot.description,
                    status = newBot.status.toEntity(),
                    chatId = newBot.chatId
                )
                call.respond(HttpStatusCode.OK, APIResponse("Bot added successfully", null))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, APIResponse(Errors.GENERAL, e.message))
            }
        }

        put("/{id}") {
            val id = call.parameters["id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, APIResponse(Errors.MISS_ID, null))
                return@put
            }

            val botDto = call.receiveNullable<CreateBotDTO>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, APIResponse(Errors.INVALID_BODY, null))
                return@put
            }

            try {
                botsService.updateBot(
                    id = id,
                    botUsername = botDto.botUsername,
                    botToken = botDto.botToken,
                    botDescription = botDto.description,
                    status = botDto.status.toEntity(),
                    chatId = botDto.chatId
                )
                call.respond(HttpStatusCode.OK, APIResponse("Bot updated successfully", null))
            } catch (e: NotFoundException) {
                call.respond(HttpStatusCode.NotFound, APIResponse(Errors.BOT_NOT_FOUND, null))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, APIResponse(Errors.GENERAL, e.message))
            }
        }

        delete("/{id}") {
            val id = call.parameters["id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, APIResponse(Errors.MISS_ID, null))
                return@delete
            }

            try {
                botsService.deleteBotById(id)
                call.respond(HttpStatusCode.OK, APIResponse("Bot deleted successfully", null))
            } catch (e: NotFoundException) {
                call.respond(HttpStatusCode.NotFound, APIResponse(Errors.BOT_NOT_FOUND, null))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, APIResponse(Errors.GENERAL, e.message))
            }
        }

    }
}