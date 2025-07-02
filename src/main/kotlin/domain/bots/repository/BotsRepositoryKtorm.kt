package dev.limebeck.openconf.domain.bots.repository


import dev.limebeck.openconf.DbConfig
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.schema.Table
import org.ktorm.schema.long
import org.ktorm.schema.uuid
import org.ktorm.schema.varchar
import java.util.*

object BotsTable : Table<Nothing>("bots") {
    val id = uuid("id").primaryKey()
    val botUsername = varchar("bot_username")
    val botToken = varchar("bot_token")
    val description = varchar("description")
    val status = varchar("status")
    val chatId = long("chat_id")
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
        botUsername = r[BotsTable.botUsername]!!,
        botToken = r[BotsTable.botToken]!!,
        description = r[BotsTable.description]!!,
        status = BotStatus.valueOf(r[BotsTable.status]!!.uppercase()),
        chatId = r[BotsTable.chatId]
    )

    override fun findAllBots(botFilter: BotFilter?): List<Bot> {
        return database
            .from(BotsTable)
            .select()
            .whereWithConditions {
                if (botFilter?.botUsername != null) {
                    it += BotsTable.botUsername like "${botFilter.botUsername}%"
                }

                if (botFilter?.status != null) {
                    it += BotsTable.status eq botFilter.status.name
                }
            }
            .map(::botsMapper)
    }

    override fun findBotById(id: UUID): Bot? {
        return database
            .from(BotsTable)
            .select()
            .where {
                BotsTable.id eq id
            }
            .map(::botsMapper)
            .firstOrNull()
    }

    override fun addBot(botUsername: String, botToken: String, description: String, status: BotStatus, chatId: Long?) {
        database.insert(BotsTable) {
            set(BotsTable.id, UUID.randomUUID())
            set(BotsTable.botToken, botToken)
            set(BotsTable.botUsername, botUsername)
            set(BotsTable.description, description)
            set(BotsTable.status, status.name)
            set(BotsTable.chatId, chatId)
        }
    }

    override fun updateBot(id: UUID, botUsername: String, botToken: String, description: String, status: BotStatus, chatId: Long?) {
        database.update(BotsTable) {
            set(BotsTable.botToken, botToken)
            set(BotsTable.botUsername, botUsername)
            set(BotsTable.description, description)
            set(BotsTable.status, status.name)
            set(BotsTable.chatId, chatId)
            where {
                BotsTable.id eq id
            }
        }
    }

    override fun deleteBotById(id: UUID) {
        database.delete(BotsTable) { BotsTable.id eq id }
    }
}
