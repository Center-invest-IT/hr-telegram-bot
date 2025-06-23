package domain

import dev.limebeck.openconf.db.DbConfiguration
import dev.limebeck.openconf.db.FlywayMigrationService
import org.testcontainers.containers.PostgreSQLContainer
import dev.limebeck.openconf.DbConfig
import dev.limebeck.openconf.QuestionId
import dev.limebeck.openconf.domain.Answer
import dev.limebeck.openconf.domain.admin.AdminId
import dev.limebeck.openconf.domain.admin.AdminsRepositoryKtorm
import dev.limebeck.openconf.domain.admin.AdminInfo
import java.util.UUID
import kotlin.math.log
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class KLPostgreSQLContainer(image: String) : PostgreSQLContainer<KPostgreSQLContainer>(image)

class AdminsRepositoryKtormTest {
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
    fun `Work with database`(){

        val repository = AdminsRepositoryKtorm(
            config = DbConfig.KtormConfig(
                url = postgres.jdbcUrl,
                driver = "org.postgresql.Driver",
                username = postgres.username,
                password = postgres.password
            )
        )

        assert(repository.getAll().isEmpty())
        { "No questions should be returned" }

        val testid = AdminId(UUID.randomUUID())

        val testadmin1 = AdminInfo(
            id = testid,
            login = "testadmin",
            passwordHash = "testpassword"
        )
        val testadmin2 = AdminInfo(
            id =  testid,
            login = "ttestadmin",
            passwordHash = "ttestpassword"
        )

        //тест add
        repository.add(testadmin1)
        assertEquals(
            repository.getAll().first(),
            AdminInfo(
                id = testadmin1.id,
                login = testadmin1.login,
                passwordHash = testadmin1.passwordHash
            )
        )

        //тест delete
        repository.delete(testadmin1.id)
        assert(repository.getAll().isEmpty())
        { "No questions should be returned" }

        //тест update
        repository.add(testadmin1)
        repository.update(testadmin2)
        assertEquals(
            repository.getAll().first(),
            AdminInfo(
                id = testadmin2.id,
                login = testadmin2.login,
                passwordHash = testadmin2.passwordHash
            )
        )
        repository.delete(testadmin1.id)
        assert(repository.getAll().isEmpty())
        { "No questions should be returned" }

        //тест поиска по login и по id
        repository.add(testadmin1)
        val foundByLogin = requireNotNull(repository.findByLogin(testadmin1.login)) {
            "Admin not found by login"
        }
        val foundById = requireNotNull(repository.findById(testadmin1.id)) {
            "Admin not found by ID"
        }
        assertEquals(
            repository.getAll().first(),
            AdminInfo(
                id = foundByLogin.id,
                login = foundByLogin.login,
                passwordHash = foundByLogin.passwordHash
            ))
        assertEquals(
            repository.getAll().first(),
            AdminInfo(
                id = foundById.id,
                login = foundById.login,
                passwordHash = foundById.passwordHash
            ))
    }
}