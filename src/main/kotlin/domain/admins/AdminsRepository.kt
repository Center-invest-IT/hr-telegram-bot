package dev.limebeck.openconf.domain.admin

import dev.inmo.tgbotapi.types.ChatId
import dev.limebeck.openconf.DbConfig
import java.util.UUID
import kotlin.uuid.Uuid

interface AdminsRepository {
    fun getAllAdmins(): List<AdminInfo>
    fun getAdminHashpass(id: AdminId): List<AdminInfo>
    fun addAdmin(admin: AdminInfo)
    fun deleteAdmin (admin: AdminInfo)
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

    override fun addAdmin(admin: AdminInfo) {
        admins[admin.id] = admin
    }

    override fun updateAdmin(admin: AdminInfo) {
        if (admins.containsKey(admin.id)) {
            admins[admin.id] = admin
        }
    }
    override fun deleteAdmin(admin: AdminInfo) {
        admins.remove(admin.id)
    }

    override fun getAdminHashpass(adminId: AdminId): List<AdminInfo> {
        return admins[adminId.uuid]?.let { listOf(it) } ?: emptyList()
    }
}

