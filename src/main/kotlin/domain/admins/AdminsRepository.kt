package dev.limebeck.openconf.domain.admin

import dev.inmo.tgbotapi.types.ChatId
import dev.limebeck.openconf.DbConfig
import java.util.UUID
import kotlin.uuid.Uuid

interface AdminsRepository {
    fun getAllAdmins(): List<AdminInfo>
    fun getAdminHashpass(id: AdminId): AdminInfo?
    fun getAdminByLogin(login: String): AdminInfo?
    fun addAdmin(admin: AdminInfo)
    fun deleteAdmin (admin: UUID)
    fun updateAdmin(admin: AdminInfo)
}

@JvmInline
value class AdminId(val uuid: UUID)

data class AdminInfo(
    val id: UUID,
    val login: String,
    val password: String
)

class AdminsRepositoryMock : AdminsRepository {

    val admins = mutableMapOf<UUID, AdminInfo>()

    override fun getAllAdmins(): List<AdminInfo> {
        return admins.values.toList()
    }

    override fun getAdminByLogin(login: String): AdminInfo? {
        return admins.values.firstOrNull { it.login == login }
    }

    override fun addAdmin(admin: AdminInfo) {
        admins[admin.id] = admin
    }

    override fun updateAdmin(admin: AdminInfo) {
        if (admins.containsKey(admin.id)) {
            admins[admin.id] = admin
        }
    }
    override fun deleteAdmin(admin: UUID) {
        admins.remove(admin)
    }

    override fun getAdminHashpass(id: AdminId): AdminInfo? {
        return admins[id.uuid]
    }
}

