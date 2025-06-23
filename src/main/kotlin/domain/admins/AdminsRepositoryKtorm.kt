package dev.limebeck.openconf.domain.admin


import dev.limebeck.openconf.DbConfig
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.schema.*
import java.util.*

object AdminsTable : Table<Nothing>("admins") {
    val id = uuid("id").primaryKey()
    val login = varchar("login")
    val password = varchar("password_hash")
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

    override fun findByLogin(login: String): AdminInfo? {
        return database
            .from(AdminsTable)
            .select()
            .where { AdminsTable.login eq login }
            .map(::AdminsMapper)
            .firstOrNull()
    }

    override fun getAll(): List<AdminInfo> {
        return database
            .from(AdminsTable)
            .select()
            .map (::AdminsMapper)
    }

    override fun add(admin: AdminInfo) {
        database.insert(AdminsTable) {
            set(AdminsTable.id, admin.id.uuid)
            set(AdminsTable.login, admin.login)
            set(AdminsTable.password, admin.passwordHash)
        }
    }

    override fun delete(id: AdminId) {
        database.delete(AdminsTable) {
            AdminsTable.id eq id.uuid
        }
    }

    override fun update(admin: AdminInfo) {
        database.update(AdminsTable) {
            set(AdminsTable.login, admin.login)
            set(AdminsTable.password, admin.passwordHash)
            where { AdminsTable.id eq admin.id.uuid }
        }
    }

    override fun findById(id: AdminId): AdminInfo? {
        return database
            .from(AdminsTable)
            .select()
            .where { AdminsTable.id eq id.uuid }
            .map(::AdminsMapper)
            .firstOrNull()
    }
    private fun AdminsMapper(row: QueryRowSet): AdminInfo = AdminInfo(
        id = AdminId(row[AdminsTable.id]!!),
        login = row[AdminsTable.login]!!,
        passwordHash = row[AdminsTable.password]!!
    )
}