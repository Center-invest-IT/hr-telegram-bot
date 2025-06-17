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

    fun deleteAdmin(id: UUID) {
        val existing = repository.getAdminHashpass(AdminId(id)).firstOrNull()
        if (existing != null) {
            repository.deleteAdmin(existing)
        }
    }

    fun updateAdmin(admin: AdminInfo) {
        repository.updateAdmin(admin)
    }

    fun login(login: String, RawPassword: String): Boolean {
        val admin = repository.getAllAdmins().firstOrNull { it.login == login }
        return admin?.let { BCrypt.checkpw(RawPassword, it.password) } ?: false }


    fun getAdminById(id: UUID): AdminInfo? {
        return repository.getAdminHashpass(AdminId(id)).firstOrNull()
    }
}