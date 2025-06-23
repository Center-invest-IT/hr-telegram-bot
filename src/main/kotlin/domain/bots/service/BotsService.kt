package dev.limebeck.openconf.domain.bots.service

import dev.limebeck.openconf.domain.bots.repository.Bot
import dev.limebeck.openconf.domain.bots.repository.BotFilter
import dev.limebeck.openconf.domain.bots.repository.BotStatus

interface BotsService {
    suspend fun getAllBots(): List<Bot>
    suspend fun getBotById(id: String): Bot?
    suspend fun addBot(botUsername: String, botToken: String, botDescription: String, status: BotStatus, chatId: Long?)
    suspend fun updateBot(id: String, botUsername: String, botToken: String, botDescription: String, status: BotStatus, chatId: Long?)
    suspend fun deleteBotById(id: String)
    suspend fun getBotByUsername(botUsername: String, botFilter: BotFilter?): List<Bot>
}