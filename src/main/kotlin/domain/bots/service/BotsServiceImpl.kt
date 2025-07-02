package dev.limebeck.openconf.domain.bots.service

import dev.limebeck.openconf.domain.bots.repository.Bot
import dev.limebeck.openconf.domain.bots.repository.BotFilter
import dev.limebeck.openconf.domain.bots.repository.BotStatus
import dev.limebeck.openconf.domain.bots.repository.BotsRepository
import io.ktor.server.plugins.*
import java.util.*

class BotsServiceImpl(
    private val botsRepository: BotsRepository
) : BotsService {
    override suspend fun getAllBots(botFilter: BotFilter?): List<Bot> {
        val bots = botsRepository.findAllBots(botFilter)
        return bots
    }

    override suspend fun getBotById(id: String): Bot? {
        val bot = botsRepository.findBotById(UUID.fromString(id))
        return bot
    }

    override suspend fun addBot(
        botUsername: String,
        botToken: String,
        botDescription: String,
        status: BotStatus,
        chatId: Long?
    ) {
        botsRepository.addBot(botUsername, botToken, botDescription, status, chatId)
    }

    override suspend fun updateBot(
        id: String,
        botUsername: String,
        botToken: String,
        botDescription: String,
        status: BotStatus,
        chatId: Long?
    ) {
        val existingBot = botsRepository.findBotById(UUID.fromString(id))
            ?: throw NotFoundException("Bot not found")

        if (existingBot.botUsername != botUsername ||
            existingBot.botToken != botToken ||
            existingBot.description != botDescription ||
            existingBot.status != status ||
            existingBot.chatId != chatId
        ) {
            botsRepository.updateBot(
                UUID.fromString(id),
                botUsername,
                botToken,
                botDescription,
                status,
                chatId
            )
        }
    }

    override suspend fun deleteBotById(id: String) {
        val existingBot = botsRepository.findBotById(UUID.fromString(id))
            ?: throw NotFoundException("Bot not found")

        botsRepository.deleteBotById(UUID.fromString(id))
    }
}