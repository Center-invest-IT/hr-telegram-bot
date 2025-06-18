package dev.limebeck.openconf.domain.admin

import org.mindrot.jbcrypt.BCrypt
import java.util.UUID

class AdminsService(
    private val repository: AdminsRepository
) {
    fun getAllAdmins(): List<AdminInfo> {
        return repository.getAllAdmins()
    }

    fun addAdminWithRawPassword(Id: UUID, rawPassword: String, login: String) {
        val hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt())
        val admin = AdminInfo(password = hash, login = login, id = Id)
        repository.addAdmin(admin)
    }

    fun deleteAdmin(id: UUID) = repository.deleteAdmin(id)

    fun updateAdmin(admin: AdminInfo) {
        val hash = BCrypt.hashpw(admin.password, BCrypt.gensalt())
        val admin = AdminInfo(password = hash, login = admin.login, id = admin.id)
        repository.updateAdmin(admin)
    }

    fun login(login: String, rawPassword: String): Boolean {
        val admin = repository.getAdminByLogin(login)
        return admin?.let { BCrypt.checkpw(rawPassword, it.password) } ?: false
    }

    fun getAdminById(id: UUID): AdminInfo? = repository.getAdminHashpass(AdminId(id))
}