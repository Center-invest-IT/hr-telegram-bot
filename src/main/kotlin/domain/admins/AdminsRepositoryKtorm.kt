package dev.limebeck.openconf.domain.admin


import dev.limebeck.openconf.DbConfig
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.schema.*
import java.util.*

object AdminsTable : Table<Nothing>("admins") {
    val id = uuid("adminid").primaryKey()
    val login = varchar("adminlogin")
    val password = varchar("hashpassword")
}

class AdminsRepositoryKtorm(
    config: DbConfig.KtormConfig
) : AdminsRepository {
    val database = Database.connect(
        url = config.url,
        driver = config.driver,
        user = config.username,
        password = config.password
    )

    override fun getAdminByLogin(login: String): AdminInfo? {
        return database
            .from(AdminsTable)
            .select()
            .where { AdminsTable.login eq login }
            .map(::AdminsMapper)
            .firstOrNull()
    }

    override fun getAllAdmins(): List<AdminInfo> {
        return database
            .from(AdminsTable)
            .select()
            .map (::AdminsMapper)
    }

    override fun addAdmin(admin: AdminInfo) {
        database.insert(AdminsTable) {
            set(AdminsTable.id, UUID.randomUUID())
            set(AdminsTable.login, admin.login)
            set(AdminsTable.password,admin.password)
        }
    }

    override fun deleteAdmin(admin: UUID) {
        database.delete(AdminsTable) {
            AdminsTable.id eq admin
        }
    }

    override fun updateAdmin(admin: AdminInfo) {
        database.update(AdminsTable) {
            set(AdminsTable.login, admin.login)
            set(AdminsTable.password, admin.password)
            where { AdminsTable.id eq admin.id }
        }
    }

    override fun getAdminHashpass(id: AdminId): AdminInfo? {
        return database
            .from(AdminsTable)
            .select()
            .where { AdminsTable.id eq id.uuid }
            .map(::AdminsMapper)
            .firstOrNull()
    }
    private fun AdminsMapper(row: QueryRowSet): AdminInfo = AdminInfo(
        id = row[AdminsTable.id]!!,
        login = row[AdminsTable.login]!!,
        password = row[AdminsTable.password]!!
   )
}