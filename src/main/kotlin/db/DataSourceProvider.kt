package dev.limebeck.openconf.db

import javax.sql.DataSource

data class DbConfiguration(
    val dbUrl: String,
    val dbDriver: String,
    val dbUsername: String,
    val dbPassword: String,
    val dbMaxPoolSize: Int = 10
)

interface DataSourceProvider {
    fun getDataSource(configuration: DbConfiguration): DataSource
}