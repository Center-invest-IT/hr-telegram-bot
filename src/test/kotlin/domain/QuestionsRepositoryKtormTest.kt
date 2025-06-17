package domain

import dev.inmo.tgbotapi.types.RawChatId
import dev.inmo.tgbotapi.types.UserId
import dev.limebeck.openconf.DbConfig
import dev.limebeck.openconf.QuestionId
import dev.limebeck.openconf.common.MockTimeProvider
import dev.limebeck.openconf.db.DbConfiguration
import dev.limebeck.openconf.db.FlywayMigrationService
import dev.limebeck.openconf.domain.Answer
import dev.limebeck.openconf.domain.QuestionsRepositoryKtorm
import dev.limebeck.openconf.domain.UserInfo
import org.testcontainers.containers.PostgreSQLContainer
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class KPostgreSQLContainer(image: String) : PostgreSQLContainer<KPostgreSQLContainer>(image)

class QuestionsRepositoryKtormTest {
    private val postgres = KPostgreSQLContainer("postgres:17-alpine")
        .withCommand("postgres -c max_connections=100")

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
    }

    @AfterTest
    fun `Clean up`() {
        postgres.stop()
    }

    @Test
    fun `Work with database`() {
        val timeProvider = MockTimeProvider(Instant.parse("2025-06-17T00:00:00Z"))

        val repository = QuestionsRepositoryKtorm(
            timeProvider = timeProvider,
            config = DbConfig.KtormConfig(
                url = postgres.jdbcUrl,
                driver = "org.postgresql.Driver",
                username = postgres.username,
                password = postgres.password
            )
        )

        assert(repository.getAllAnswers().isEmpty()) { "No questions should be returned" }

        val testQuestionId = QuestionId("testId")

        val testUser = UserInfo(
            userId = UserId(RawChatId(0)),
            username = "TEST"
        )

        val testAnswer = "Test"

        repository.addAnswer(
            userInfo = testUser,
            questionId = testQuestionId,
            answer = testAnswer
        )

        assertEquals(
            repository.getAllAnswers().first(),
            Answer(
                questionId = testQuestionId,
                answer = testAnswer,
                userInfo = testUser,
                dateTime = timeProvider.getCurrent()
            )
        )
    }
}