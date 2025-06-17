package dev.limebeck.openconf.domain.admin

import org.junit.jupiter.api.*
import org.mindrot.jbcrypt.BCrypt
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdminsServiceTest {

    private lateinit var repository: AdminsRepositoryMock
    private lateinit var service: AdminsService

    @BeforeAll
    fun setup() {
        repository = AdminsRepositoryMock()
        service = AdminsService(repository)
    }

    @Test
    fun testAddAdminWithRawPasswordAndLogin() {
        val id = UUID.randomUUID()
        val rawPassword = "secure123"
        val login = "admin1"

        service.addAdminWithRawPassword(id, rawPassword, login)
        val addedAdmin = repository.getAdminHashpass(AdminId(id)).firstOrNull()

        assertNotNull(addedAdmin)
        assertEquals(login, addedAdmin?.login)
        assertTrue(BCrypt.checkpw(rawPassword, addedAdmin?.password))
    }

    @Test
    fun testLoginSuccess() {
        val id = UUID.randomUUID()
        val rawPassword = "testpass"
        val login = "admin2"
        service.addAdminWithRawPassword(id, rawPassword, login)

        val success = service.login(login, rawPassword)
        assertTrue(success)
    }

    @Test
    fun testLoginFailure() {
        val success = service.login("nonexistent", "wrongpass")
        assertFalse(success)
    }

    @Test
    fun testDeleteAdmin() {
        val id = UUID.randomUUID()
        val login = "admin3"
        val pass = "tobedeleted"
        service.addAdminWithRawPassword(id, pass, login)

        service.deleteAdmin(id)
        val deleted = repository.getAdminHashpass(AdminId(id))
        assertTrue(deleted.isEmpty())
    }
}