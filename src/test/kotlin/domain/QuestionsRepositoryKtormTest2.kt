package domain

import dev.limebeck.openconf.DbConfig
import dev.limebeck.openconf.common.MockTimeProvider
import dev.limebeck.openconf.common.TimeProvider
import dev.limebeck.openconf.db.DbConfiguration
import dev.limebeck.openconf.db.FlywayMigrationService
import dev.limebeck.openconf.domain.questions.QuestionRepository
import dev.limebeck.openconf.domain.questions.QuestionRepositoryKtorm
import dev.limebeck.openconf.domain.questions.UserInfo
import java.time.Instant
import kotlin.test.*

class QuestionsRepositoryKtormTest2 {
    private val postgres = KPostgreSQLContainer("postgres:17-alpine")
        .withCommand("postgres -c max_connections=100")

    private lateinit var repository: QuestionRepository

    @BeforeTest
    fun `Apply migrations`() {

        val timeProvider = MockTimeProvider(Instant.parse("2025-06-17T00:00:00Z"))

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

        repository = QuestionRepositoryKtorm(
            timeProvider = timeProvider,
            config = DbConfig.KtormConfig(
                url = postgres.jdbcUrl,
                driver = "org.postgresql.Driver",
                username = postgres.username,
                password = postgres.password,
            )
        )
    }

    @AfterTest
    fun `Clean up` ()
    {
        postgres.stop()
    }

    @Test
    fun `Test addQuestion and findAllQuestions`() {
        repository.addQuestion("Test question ?")
        repository.addQuestion("Test Question 2?")

        val questions = repository.findAllQuestions()
        assertEquals(2, questions.size)
    }

    @Test
    fun `Test updateQuestion and deleteQuestion`() {
        repository.addQuestion("Test question ?")

        val questionId = repository.findAllQuestions().first().id

        repository.updateQuestion(
            id = questionId,
            question = "Updated question ?"
        )

        val updatedQuestion = repository.findAllQuestions().first()

        assertEquals(updatedQuestion.question, "Updated question ?")

        repository.deleteQuestion(updatedQuestion.id)
        assertNull(repository.findAllQuestions().firstOrNull())
    }

    @Test
    fun `Test addAnswer, findUserAnswers and findAllAnswers`() {
        repository.addQuestion("Test question ?")

        val questionId = repository.findAllQuestions().first().id
        val userInfo = UserInfo(12345L, "user")

        repository.addAnswer(
            userInfo = userInfo,
            questionId = questionId,
            answer = "answer 1"
        )

        repository.addAnswer(
            userInfo = userInfo,
            questionId = questionId,
            answer = "answer 2"
        )

        val userAnswers = repository.findUserAnswers(12345L)
        assertEquals(2, userAnswers.size)

        //------

        repository.addQuestion("Test question2 ?")
        val questionId2 = repository.findAllQuestions().first().id
        val userInfo2 = UserInfo(123456L, "user2")

        repository.addAnswer(
            userInfo = userInfo2,
            questionId = questionId2,
            answer = "answer 3"
        )

        val allAnswers = repository.findAllAnswers()
        assertEquals(3, allAnswers.size)
    }
}