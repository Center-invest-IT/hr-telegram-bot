package dev.limebeck.openconf.domain.bots.controller

import dev.limebeck.openconf.domain.bots.repository.BotFilter
import dev.limebeck.openconf.domain.bots.repository.BotStatus
import dev.limebeck.openconf.domain.bots.service.BotsService
import dev.limebeck.openconf.domain.bots.web.CreateBotDTO
import dev.limebeck.openconf.domain.bots.web.mapper.toDTO
import dev.limebeck.openconf.domain.bots.web.mapper.toEntity
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
                val botUsername = call.request.queryParameters["botUsername"]
                val statusParam = call.request.queryParameters["status"]

                val status = try {
                    statusParam?.uppercase()?.let { BotStatus.valueOf(it) }
                } catch (e: IllegalArgumentException) {
                    null
                }

                val filter = BotFilter(
                    botUsername = botUsername,
                    status = status
                )

                val bots = botsService.getAllBots(filter).map { it.toDTO() }
                call.respond(HttpStatusCode.OK, bots)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        get("/{botId}") {
            val id = call.parameters["botId"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            try {
                val bot = botsService.getBotById(id)
                if (bot == null) {
                    call.respond(HttpStatusCode.NotFound)
                } else {
                    call.respond(HttpStatusCode.OK, bot.toDTO())
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        post {
            val createBotDTO = call.receiveNullable<CreateBotDTO>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            try {
                val newBot = botsService.addBot(
                    botUsername = createBotDTO.botUsername,
                    botToken = createBotDTO.botToken,
                    botDescription = createBotDTO.description,
                    status = createBotDTO.status.toEntity(),
                    chatId = createBotDTO.chatId
                )
                call.respond(HttpStatusCode.Created, newBot.toDTO())
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        put("/{botId}") {
            val id = call.parameters["botId"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }

            val botDto = call.receiveNullable<CreateBotDTO>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
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
                call.respond(HttpStatusCode.NoContent)
            } catch (e: NotFoundException) {
                call.respond(HttpStatusCode.NotFound)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        delete("/{botId}") {
            val id = call.parameters["botId"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }

            try {
                botsService.deleteBotById(id)
                call.respond(HttpStatusCode.NoContent)
            } catch (e: NotFoundException) {
                call.respond(HttpStatusCode.NotFound)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }
    }
}