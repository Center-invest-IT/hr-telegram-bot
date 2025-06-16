package dev.limebeck.openconf.domain.admin

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.UUID

class AdminsRepositoryMockTest {

    private val repo = AdminsRepositoryMock()

    @Test
    fun testAddAndGetAdmin() {
        val id = UUID.randomUUID()
        val admin = AdminInfo(id = id, login = "admin", password = "hashed")

        repo.addAdmin(admin)

        val admins = repo.getAllAdmins()
        assertEquals(1, admins.size)
        assertEquals("admin", admins.first().login)
    }

    @Test
    fun testDeleteAdmin() {
        val id = UUID.randomUUID()
        val admin = AdminInfo(id, "admin", "hash")
        repo.addAdmin(admin)
        repo.deleteAdmin(admin)
        assertTrue(repo.getAllAdmins().isEmpty())
    }
}