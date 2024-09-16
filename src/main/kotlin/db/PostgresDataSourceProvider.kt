package dev.limebeck.openconf.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.postgresql.ds.PGSimpleDataSource
import java.security.InvalidParameterException
import javax.sql.DataSource

object PostgresDataSourceProvider : DataSourceProvider {
    override fun getDataSource(
        configuration: DbConfiguration
    ): DataSource {
        return if (configuration.dbMaxPoolSize > 1) {
            val hikariConfig = HikariConfig().apply {
                jdbcUrl = configuration.dbUrl
                driverClassName = configuration.dbDriver
                username = configuration.dbUsername
                password = configuration.dbPassword
                maximumPoolSize = configuration.dbMaxPoolSize
            }

            HikariDataSource(hikariConfig)
        } else {
            when (configuration.dbDriver) {
                "org.postgresql.Driver" -> {
                    PGSimpleDataSource().apply {
                        setUrl(configuration.dbUrl)
                        user = configuration.dbUsername
                        password = configuration.dbPassword
                    }
                }

                else -> throw InvalidParameterException("<c60d1fbb> Incompatible dbDriver parameter '${configuration.dbDriver}. Allowed only 'org.postgresql.Driver'")
            }
        }
    }
}
