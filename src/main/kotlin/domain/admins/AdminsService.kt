package dev.limebeck.openconf.domain.admin

import dev.inmo.tgbotapi.types.UserId
import org.mindrot.jbcrypt.BCrypt
import java.util.UUID

class AdminsService(
    private val repository: AdminsRepository
) {
    fun getAllAdmins(): List<AdminInfo> {
        return repository.getAll()
    }

    fun addAdminWithRawPassword(rawPassword: String, login: String): AdminInfo {
        val id = UUID.randomUUID()
        val hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt())
        val admin = AdminInfo(id = AdminId(id) , login = login,passwordHash = hash)
        repository.add(admin)
        return admin
    }

    fun deleteAdmin(id: UUID) = repository.delete(AdminId(id))

    fun updateAdmin(admin: AdminInfo) {
        val hash = BCrypt.hashpw(admin.passwordHash, BCrypt.gensalt())
        val admin = AdminInfo(passwordHash = hash, login = admin.login, id = admin.id)
        repository.update(admin)
    }

    fun login(login: String, rawPassword: String): AdminInfo? {
        val admin = repository.findByLogin(login)
        return if (admin != null && BCrypt.checkpw(rawPassword, admin.passwordHash)) {
            admin
        } else {
            null
        }
    }

    fun getAdminById(id: UUID): AdminInfo? = repository.findById(AdminId(id))

    fun getAdminId(login: String): UUID? {
        val admin = repository.findByLogin(login)
        return if (admin != null) {
            admin.id.uuid
        } else {
            null
        }
    }
}