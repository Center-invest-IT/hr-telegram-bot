package dev.limebeck.openconf.domain.bots.repository

import java.util.*


interface BotsRepository {
    fun getAllBots(): List<Bot>
    fun addBot(botUserName: String, botToken: String)
    fun updateBot(id: UUID, botUserName: String, botToken: String)
    fun deleteBotById(id: UUID)
    fun findBotByUsername(botUsername: String): List<Bot>
    fun setChannelToBot(botUserName: String, chatId: Long)
    fun updateBotChannel(id: UUID, botUserName: String, chatId: Long)
    fun deleteChannelFromBotBytUsername(botUsername: String)
}

data class Bot(
    val id: UUID,
    val botUserName: String,
    val botToken: String,
    val description: String,
    val status: Boolean
)

data class BotsChats(
    val id : UUID,
    val botUserName: String,
    val chatId: Long,
)


