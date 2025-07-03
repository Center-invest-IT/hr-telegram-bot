package domain

import dev.inmo.tgbotapi.types.RawChatId
import dev.limebeck.openconf.DbConfig
import dev.inmo.tgbotapi.types.UserId
import dev.limebeck.openconf.QuestionId
import dev.limebeck.openconf.common.MockTimeProvider
import dev.limebeck.openconf.db.DbConfiguration
import dev.limebeck.openconf.db.FlywayMigrationService
import dev.limebeck.openconf.domain.QuestionsRepository
import dev.limebeck.openconf.domain.QuestionsRepositoryKtorm
import dev.limebeck.openconf.domain.UserInfo
import dev.limebeck.openconf.domain.questions.repository.QuestionRepository
import dev.limebeck.openconf.domain.questions.repository.QuestionRepositoryKtorm
import java.time.Instant
import java.util.*
import kotlin.test.*

class QuestionsRepositoryKtormTest2 {
    private val postgres = KPostgreSQLContainer("postgres:17-alpine")
        .withCommand("postgres -c max_connections=100")

    private lateinit var repository: QuestionRepository
    private lateinit var repositoryAns: QuestionsRepository

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

        repositoryAns = QuestionsRepositoryKtorm(
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
    fun `Test addQuestion and findQuestionsByBotId`() {
        repository.addQuestion(
            "Test Question 1?",
            UUID.fromString("16763be4-6022-406e-a950-fcd5018633ca")
        )

        repository.addQuestion(
            "Test Question 2?",
            UUID.fromString("16763be4-6022-406e-a950-fcd5018633ca")
        )

        val questions = repository.findQuestionsByBotId(UUID.fromString("16763be4-6022-406e-a950-fcd5018633ca"))
        assertEquals(2, questions.size)
    }

    @Test
    fun `Test updateQuestion and deleteQuestion`() {
        repository.addQuestion(
            "Test Question 1?",
            UUID.fromString("16763be4-6022-406e-a950-fcd5018633ca")
        )

        val questionId = repository.
            findQuestionsByBotId(
                UUID.fromString("16763be4-6022-406e-a950-fcd5018633ca")
            ).first().id

        repository.updateQuestion(
            questionId,
            "Test updated Question1?"
        )

        val question = repository.
        findQuestionsByBotId(
            UUID.fromString("16763be4-6022-406e-a950-fcd5018633ca")
        ).first().question

        assertEquals("Test updated Question1?", question)

        repository.deleteQuestion(questionId)

        val questions = repository.
        findQuestionsByBotId(
            UUID.fromString("16763be4-6022-406e-a950-fcd5018633ca")
        )

        assertEquals(0, questions.size)
    }

    @Test
    fun `Test findQuestionAnswers and findAllAnswersByBotId`() {
        val botUUID = UUID.fromString("16763be4-6022-406e-a950-fcd5018633ca")

        repository.addQuestion(
            "Test Question 1?",
            botUUID
        )

        repository.addQuestion(
            "Test Question 2?",
            botUUID
        )

        val testUser = UserInfo(
            userId = UserId(RawChatId(0)),
            username = "TEST"
        )

        val testUser2 = UserInfo(
            userId = UserId(RawChatId(1)),
            username = "TEST2"
        )

        val questionId =
            repository.findQuestionsByBotId(
                botUUID
            ).first().id

        val questionIdVal = QuestionId(questionId.toString())

        repositoryAns.addAnswer(
            userInfo = testUser,
            questionId = questionIdVal,
            answer = "testAnswer"
        )

        repositoryAns.addAnswer(
            userInfo = testUser2,
            questionId = questionIdVal,
            answer = "testAnswer 2"
        )

        val questionAnswers = repository.findQuestionAnswers(
            questionId, botUUID
        )

        assertEquals(2, questionAnswers.size)

        val questionId2 = repository.findQuestionsByBotId(
            botUUID
        )[1].id

        val questionIdVal2 = QuestionId(questionId2.toString())

        repositoryAns.addAnswer(
            userInfo = testUser,
            questionId = questionIdVal2,
            answer = "test Answer for q2"
        )

        val allAnswers = repository.findAllAnswersByBotId(botUUID)
        assertEquals(3, allAnswers.size)

        println("questionAnswers: $questionAnswers")
        println("-".repeat(15))
        println("allAnswers: $allAnswers")
    }
}