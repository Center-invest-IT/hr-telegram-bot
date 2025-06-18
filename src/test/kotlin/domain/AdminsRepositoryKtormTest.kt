package domain

import com.benasher44.uuid.uuid
import dev.inmo.tgbotapi.requests.stickers.AddStickerToSet
import dev.limebeck.openconf.db.DbConfiguration
import dev.limebeck.openconf.db.FlywayMigrationService
import org.testcontainers.containers.PostgreSQLContainer
import dev.inmo.tgbotapi.types.RawChatId
import dev.inmo.tgbotapi.types.UserId
import dev.limebeck.openconf.DbConfig
import dev.limebeck.openconf.domain.admin.AdminId
import dev.limebeck.openconf.domain.admin.AdminsRepository
import dev.limebeck.openconf.domain.admin.AdminsRepositoryKtorm
import dev.limebeck.openconf.domain.admin.AdminInfo
import dev.limebeck.openconf.domain.admin.AdminsTable.id
import java.util.UUID
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

        assert(repository.getAllAdmins().isEmpty())

        { "No questions should be returned" }

        val testid = UUID.randomUUID()
        val ttestid = UUID.randomUUID()

        val testadmin = AdminInfo(
            id = testid,
            login = "testadmin",
            password = "testpassword"
        )
        val ttestadmin = AdminInfo(
            id = ttestid,
            login = "ttestadmin",
            password = "ttestpassword"
        )

        repository.addAdmin(ttestadmin)

        assertEquals(testadmin.id, testid)
        assertEquals(testadmin.login, "testadmin")
        assertEquals(testadmin.password, "testpassword")

        val allAfterDelete = repository.getAllAdmins()
        repository.deleteAdmin(ttestadmin.id)
        println(repository.getAllAdmins())
    }
}