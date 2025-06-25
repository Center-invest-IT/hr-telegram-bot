package dev.limebeck.openconf.domain.admin

import java.util.*

interface AdminsRepository {
    fun getAll(): List<AdminInfo>
    fun findById(id: AdminId): AdminInfo?
    fun findByLogin(login: String): AdminInfo?
    fun add(admin: AdminInfo)
    fun delete(id: AdminId)
    fun update(admin: AdminInfo)
}

@JvmInline
value class AdminId(
    val uuid: UUID
)

data class AdminInfo(
    val id: AdminId,
    val login: String,
    val passwordHash: String
)

class AdminsRepositoryMock : AdminsRepository {

    val admins = mutableMapOf<AdminId, AdminInfo>()

    override fun getAll(): List<AdminInfo> {
        return admins.values.toList()
    }

    override fun findByLogin(login: String): AdminInfo? {
        return admins.values.firstOrNull { it.login == login }
    }

    override fun add(admin: AdminInfo) {
        admins[admin.id] = admin
    }

    override fun update(admin: AdminInfo) {
        if (admins.containsKey(admin.id)) {
            admins[admin.id] = admin
        }
    }

    override fun delete(id: AdminId) {
        admins.remove(id)
    }

    override fun findById(id: AdminId): AdminInfo? {
        return admins[id]
    }
}

