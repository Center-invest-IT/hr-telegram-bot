package domain

import dev.limebeck.openconf.DbConfig
import dev.limebeck.openconf.db.DbConfiguration
import dev.limebeck.openconf.db.FlywayMigrationService
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
    fun `Clean up` ()
    {
        postgres.stop()
    }

    @Test
    fun `Test addBot and findAllBots`()
    {
        assertTrue(repository.findAllBots().isEmpty())

        repository.addBot(
            botUsername = "test_bot",
            botToken = "test_token",
            description = "Test bot",
            status = true
        )

        repository.addBot(
            botUsername = "test_bot2",
            botToken = "test_token2",
            description = "Test bot2",
            status = true
        )

        val bots = repository.findAllBots()
        assertEquals(2, bots.size)
        assertEquals("test_bot", bots.first().botUserName)
    }

    @Test
    fun `Test findBotById`()
    {
        repository.addBot(
            botUsername = "test_bot",
            botToken = "test_token",
            description = "Test bot",
            status = true
        )

        val botId = repository.findAllBots()[0].id

        val bot = repository.findBotById(botId)
        assertNotNull(bot)
        assertEquals("test_bot", bot.botUserName)

        assertNull(repository.findBotById(UUID.randomUUID()))
    }

    @Test
    fun `Test updateBot`()
    {
        repository.addBot(
            botUsername = "old_bot",
            botToken = "old_token",
            description = "Old bot",
            status = true
        )

        val botId = repository.findAllBots()[0].id

        repository.updateBot(
            id = botId,
            botUsername = "updated_bot",
            botToken = "updated_token",
            description = "Updated bot",
            status = false
        )

        val updatedBot = repository.findBotById(botId)
        assertNotNull(updatedBot)
        assertEquals("updated_bot", updatedBot.botUserName)
        assertEquals(false, updatedBot.status)
    }

    @Test
    fun `Test deleteBotById`()
    {
        repository.addBot(
            botUsername = "to_delete_bot",
            botToken = "to_delete_token",
            description = "To delete",
            status = true
        )

        val botId = repository.findAllBots()[0].id

        repository.deleteBotById(botId)

        assertNull(repository.findBotById(botId))
    }

    @Test
    fun `Test findBotByUsername`()
    {
        repository.addBot(
            botUsername = "bot_one",
            botToken = "token_one",
            description = "Bot one",
            status = true
        )

        repository.addBot(
            botUsername = "bot_two",
            botToken = "token_two",
            description = "Bot two",
            status = false
        )

        val bots = repository.findBotByUsername("bot")

        assertEquals(2, bots.size)
    }

    @Test
    fun `Test setChannelToBot, deleteChannelFromBotBytUsername, updateBotChannel, findBotChannelByBotUsername`()
    {
        repository.addBot(
            botUsername = "channel_bot",
            botToken = "channel_token",
            description = "Bot with channel",
            status = true
        )

        repository.setChannelToBot(
            botUserName = "channel_bot",
            chatId = 123456789L
        )

        val channelBot = repository.findBotChannelByBotUsername("channel_bot")
        assertNotNull(channelBot)

        val botChannelId = channelBot.id

        repository.updateBotChannel(
            id = botChannelId,
            botUsername = "channel_bot",
            chatId = 987654321L
        )

        val newChannelBot = repository.findBotChannelByBotUsername("channel_bot")
        if (newChannelBot != null) {
            assertEquals(newChannelBot.chatId, 987654321)
        }

        repository.deleteChannelFromBotBytUsername("channel_bot")
        assertNull(repository.findBotChannelByBotUsername("channel_bot"))
    }

}