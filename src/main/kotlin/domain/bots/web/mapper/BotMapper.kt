package dev.limebeck.openconf.domain.bots.web.mapper

import dev.limebeck.openconf.domain.bots.repository.Bot
import dev.limebeck.openconf.domain.bots.repository.BotStatus
import dev.limebeck.openconf.domain.bots.web.BotDTO
import dev.limebeck.openconf.domain.bots.web.BotStatusDTO

fun Bot.toDTO(): BotDTO {
    return BotDTO(
        id = this.id.toString(),
        botUsername = this.botUsername,
        botToken = this.botToken,
        description = this.description,
        status = when (this.status) {
            BotStatus.ACTIVE -> BotStatusDTO.ACTIVE
            BotStatus.INACTIVE -> BotStatusDTO.INACTIVE
        },
        chatId = this.chatId
    )
}

fun BotStatusDTO.toEntity(): BotStatus = when (this) {
    BotStatusDTO.ACTIVE -> BotStatus.ACTIVE
    BotStatusDTO.INACTIVE -> BotStatus.INACTIVE
}