package dev.limebeck.openconf.domain.bots.repository

import java.util.*


interface BotsRepository {
    fun findAllBots(): List<Bot>
    fun findBotById(id: UUID): Bot?
    fun addBot(botUserName: String, botToken: String, description: String, status: Boolean)
    fun updateBot(id: UUID, botUserName: String, botToken: String, description: String, status: Boolean)
    fun deleteBotById(id: UUID)
    fun findBotByUsername(botUsername: String): List<Bot>
    fun setChannelToBot(botUserName: String, chatId: Long)
    fun updateBotChannel(id: UUID, botUsername: String, chatId: Long)
    fun deleteChannelFromBotBytUsername(botUsername: String)
}

data class Bot(
    val id: UUID,
    val botUserName: String,
    val botToken: String,
    val description: String,
    val status: Boolean
)


