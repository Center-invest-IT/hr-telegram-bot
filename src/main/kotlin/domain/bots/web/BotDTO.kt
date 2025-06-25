package dev.limebeck.openconf.domain.bots.web

import kotlinx.serialization.Serializable

@Serializable
data class CreateBotDTO(
    val botUsername: String,
    val botToken: String,
    val description: String,
    val status: BotStatusDTO,
    val chatId: Long?
)

@Serializable
enum class BotStatusDTO {
    ACTIVE,
    INACTIVE
}

@Serializable
data class BotDTO(
    val id: String,
    val botUsername: String,
    val botToken: String,
    val description: String,
    val status: BotStatusDTO,
    val chatId: Long?
)
