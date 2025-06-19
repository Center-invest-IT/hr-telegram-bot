package domain

import dev.limebeck.openconf.DbConfig
import dev.limebeck.openconf.db.DbConfiguration
import dev.limebeck.openconf.db.FlywayMigrationService
import dev.limebeck.openconf.domain.bots.repository.BotFilter
import dev.limebeck.openconf.domain.bots.repository.BotStatus
import dev.limebeck.openconf.domain.bots.repository.BotsRepositoryKtorm
import java.util.*
import kotlin.test.*


class BotsRepositoryKtormTest {
    private val postgres = KPostgreSQLContainer("postgres:17-alpine")
        .withCommand("postgres -c max_connections=100")

    private lateinit var repository: BotsRepositoryKtorm

    @BeforeTest
    fun `Apply migrations`() {
        postgres.start()
        val flyway = FlywayMigrationService(
            configuration = DbConfiguration(
                dbUrl = postgres.jdbcUrl,
                dbDriver = "org.postgresql.Driver",
                dbUsername = postgres.username,
                dbPassword = postgres.password,
            )
        )
        flyway.migrate()

        repository = BotsRepositoryKtorm(
            config = DbConfig.KtormConfig(
                url = postgres.jdbcUrl,
                driver = "org.postgresql.Driver",
                username = postgres.username,
                password = postgres.password
            )
        )
    }

    @AfterTest
    fun `Clean up` () {
        postgres.stop()
    }

    @Test
    fun `Test addBot and findAllBots`() {
        assertTrue(repository.findAllBots().isEmpty())

        repository.addBot(
            botUsername = "test_bot",
            botToken = "test_token",
            description = "Test bot",
            status = BotStatus.ACTIVE,
            chatId = 12345L
        )

        repository.addBot(
            botUsername = "test_bot2",
            botToken = "test_token2",
            description = "Test bot2",
            status = BotStatus.ACTIVE,
            chatId = 12345L
        )

        val bots = repository.findAllBots()
        assertEquals(2, bots.size)
        assertEquals("test_bot", bots.first().botUsername)
    }

    @Test
    fun `Test findBotById`() {
        repository.addBot(
            botUsername = "test_bot",
            botToken = "test_token",
            description = "Test bot",
            status = BotStatus.ACTIVE,
            chatId = 12345L
        )

        val botId = repository.findAllBots()[0].id

        val bot = repository.findBotById(botId)
        assertNotNull(bot)
        assertEquals("test_bot", bot.botUsername)

        assertNull(repository.findBotById(UUID.randomUUID()))
    }

    @Test
    fun `Test updateBot`() {
        repository.addBot(
            botUsername = "old_bot",
            botToken = "old_token",
            description = "Old bot",
            status = BotStatus.ACTIVE,
            chatId = 12345L
        )

        val botId = repository.findAllBots()[0].id

        repository.updateBot(
            id = botId,
            botUsername = "updated_bot",
            botToken = "updated_token",
            description = "Updated bot",
            status = BotStatus.INACTIVE,
            chatId = null
        )

        val updatedBot = repository.findBotById(botId)
        assertNotNull(updatedBot)
        assertEquals("updated_bot", updatedBot.botUsername)
        assertEquals(BotStatus.INACTIVE, updatedBot.status)
    }

    @Test
    fun `Test deleteBotById`() {
        repository.addBot(
            botUsername = "to_delete_bot",
            botToken = "to_delete_token",
            description = "To delete",
            status = BotStatus.ACTIVE,
            chatId = 12345L
        )

        val botId = repository.findAllBots()[0].id

        repository.deleteBotById(botId)

        assertNull(repository.findBotById(botId))
    }

    @Test
    fun `Test findBotsByUsername`() {
        repository.addBot(
            botUsername = "bot_one",
            botToken = "token_one",
            description = "Bot one",
            status = BotStatus.ACTIVE,
            chatId = 123456L
        )

        repository.addBot(
            botUsername = "bot_two",
            botToken = "token_two",
            description = "Bot two",
            status = BotStatus.INACTIVE,
            chatId = 123456789L
        )

        repository.addBot(
            botUsername = "xxxx",
            botToken = "token_three",
            description = "Bot three",
            status = BotStatus.INACTIVE,
            chatId = 123456789L
        )

        val bots = repository.findBotsByUsername("bot", BotFilter(status = BotStatus.ACTIVE))

        assertEquals(1, bots.size)

        val botsWithNullFilter = repository.findBotsByUsername("bot", null)
        assertEquals(2, botsWithNullFilter.size)
    }
}