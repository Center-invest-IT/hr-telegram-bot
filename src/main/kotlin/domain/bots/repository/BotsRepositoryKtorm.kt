package dev.limebeck.openconf.domain.bots.repository


import dev.limebeck.openconf.DbConfig
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.schema.*
import java.util.*

object BotsTable : Table<Nothing>("bots") {
    val id = uuid("id").primaryKey()
    val botUsername = varchar("bot_username")
    val botToken = varchar("bot_token")
    val description = varchar("description")
    val status = boolean("status")
}

object BotsChatsTable : Table<Nothing>("bots_chats") {
    val id = uuid("id").primaryKey()
    val chatId = long("chat_id")
    val botUsername = varchar("bot_username")
}

class BotsRepositoryKtorm(
    config: DbConfig.KtormConfig
) : BotsRepository {
    private val database = Database.connect(
        url = config.url,
        driver = config.driver,
        user = config.username,
        password = config.password
    )

    private fun botsMapper(r: QueryRowSet) = Bot(
        id = r[BotsTable.id]!!,
        botUserName = r[BotsTable.botUsername]!!,
        botToken = r[BotsTable.botToken]!!,
        description = r[BotsTable.description]!!,
        status = r[BotsTable.status]!!
    )

    override fun findAllBots(): List<Bot> {
        return database
            .from(BotsTable)
            .select()
            .map(::botsMapper)
    }

    override fun findBotById(id: UUID): Bot? {
        return database
            .from(BotsTable)
            .select()
            .where {
                BotsTable.id eq id
            }
            .map { row ->
                Bot(
                    id = row[BotsTable.id]!!,
                    botUserName = row[BotsTable.botUsername]!!,
                    botToken = row[BotsTable.botToken]!!,
                    description = row[BotsTable.description]!!,
                    status = row[BotsTable.status]!!
                )
            }
            .firstOrNull()
    }

    override fun addBot(botUsername: String, botToken: String, description: String, status: Boolean) {
        database.insert(BotsTable) {
            set(BotsTable.id, UUID.randomUUID())
            set(BotsTable.botToken, botToken)
            set(BotsTable.botUsername, botUsername)
            set(BotsTable.description, description)
            set(BotsTable.status, status)
        }
    }

    override fun updateBot(id: UUID, botUsername: String, botToken: String, description: String, status: Boolean) {
        database.update(BotsTable) {
            set(BotsTable.botToken, botToken)
            set(BotsTable.botUsername, botUsername)
            set(BotsTable.description, description)
            set(BotsTable.status, status)
            where {
                BotsTable.id eq id
            }
        }
    }

    override fun deleteBotById(id: UUID) {
        database.delete(BotsTable) { BotsTable.id eq id }
    }

    override fun findBotByUsername(botUsername: String): List<Bot> {
        return database
            .from(BotsTable)
            .select()
            .where {
                BotsTable.botUsername like "$botUsername%"
            }
            .map(::botsMapper)
            .take(10)
    }

    override fun setChannelToBot(botUserName: String, chatId: Long) {
        database.insert(BotsChatsTable) {
            set(BotsChatsTable.id, UUID.randomUUID())
            set(BotsChatsTable.botUsername, botUserName)
            set(BotsChatsTable.chatId, chatId)
        }

    }

    override fun updateBotChannel(id: UUID, botUsername: String, chatId: Long) {
        database.update(BotsChatsTable) {
            set(BotsChatsTable.botUsername, botUsername)
            set(BotsChatsTable.chatId, chatId)
            where {
                BotsChatsTable.id eq id
            }
        }
    }

    override fun deleteChannelFromBotBytUsername(botUsername: String) {
        database.delete(BotsChatsTable) { BotsChatsTable.botUsername eq botUsername }
    }

    override fun findBotChannelByBotUsername(botUsername: String): BotChannel? {
        return database
            .from(BotsChatsTable)
            .select()
            .where {
                BotsChatsTable.botUsername eq botUsername
            }
            .map { row ->
                BotChannel(
                    id = row[BotsChatsTable.id]!!,
                    chatId = row[BotsChatsTable.chatId]!!,
                    botUsername = row[BotsChatsTable.botUsername]!!
                )
            }
            .firstOrNull()
    }
}
