package dev.limebeck.openconf.domain.bots.repository


import dev.limebeck.openconf.DbConfig
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.schema.*
import java.util.*

object BotsTable: Table<Nothing>("bots") {
    val id = uuid("id").primaryKey()
    val botUsername = varchar("bot_username")
    val botToken = varchar("bot_token")
    val description = varchar("description")
    val status = boolean("status")
}

object BotsChatsTable: Table<Nothing>("bots_chats") {
    val id = uuid("id").primaryKey()
    val chatId = long("chat_id")
    val botUsername = varchar("bot_username")
}

class BotsRepositoryKtorm(
    config: DbConfig.KtormConfig
): BotsRepository {
    val database = Database.connect(
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

    override fun getAllBots(): List<Bot> {
        return database
            .from(BotsTable)
            .select()
            .map (::botsMapper)
    }

    override fun addBot(botUserName: String, botToken: String) {
        database.insert(BotsTable) {
            set(BotsTable.id, UUID.randomUUID())
            set(BotsTable.botToken, botToken)
            set(BotsTable.botUsername, botUserName)
        }
    }

    override fun updateBot(id: UUID, botUserName: String, botToken: String) {
        database.update(BotsTable) {
            set(BotsTable.botToken, botToken)
            set(BotsTable.botUsername, botUserName)
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
            .limit(10)
            .map(::botsMapper)
    }

    override fun setChannelToBot(botUserName: String, chatId: Long) {
        database.insert(BotsChatsTable) {
            set(BotsChatsTable.id, UUID.randomUUID())
            set(BotsChatsTable.botUsername, botUserName)
            set(BotsChatsTable.chatId, chatId)
        }

    }

    override fun updateBotChannel(id: UUID, botUserName: String, chatId: Long) {
        database.update(BotsChatsTable) {
            set(BotsChatsTable.botUsername, botUserName)
            set(BotsChatsTable.chatId, chatId)
            where {
                BotsChatsTable.id eq id
            }
        }
    }

    override fun deleteChannelFromBotBytUsername(botUsername: String) {
        database.delete(BotsChatsTable) { BotsChatsTable.botUsername eq botUsername }
    }
}
