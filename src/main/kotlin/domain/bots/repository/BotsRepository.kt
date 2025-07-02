package dev.limebeck.openconf.domain.bots.repository

import java.util.*


interface BotsRepository {
    fun findAllBots(botFilter: BotFilter? = null): List<Bot>
    fun findBotById(id: UUID): Bot?
    fun addBot(botUsername: String, botToken: String, description: String, status: BotStatus, chatId: Long?): Bot
    fun updateBot(id: UUID, botUsername: String, botToken: String, description: String, status: BotStatus, chatId: Long?)
    fun deleteBotById(id: UUID)
}

data class Bot(
    val id: UUID,
    val botUsername: String,
    val botToken: String,
    val description: String,
    val chatId: Long?,
    val status: BotStatus
)

enum class BotStatus {
    ACTIVE,
    INACTIVE
}

data class BotFilter(
    val status: BotStatus? = null,
    val botUsername: String? = null
)



