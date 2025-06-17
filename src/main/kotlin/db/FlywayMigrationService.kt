package dev.limebeck.openconf.db

import dev.limebeck.openconf.common.debug
import dev.limebeck.openconf.common.error
import dev.limebeck.openconf.common.info
import dev.limebeck.openconf.common.logger
import org.flywaydb.core.Flyway

class FlywayMigrationService(
    configuration: DbConfiguration,
    dataSourceProvider: DataSourceProvider = PostgresDataSourceProvider,
    private val clean: Boolean = false,
    private val migrationContextCreator: (() -> MigrationContext)? = null,
) : MigrationService {
    companion object {
        private val logger = FlywayMigrationService::class.logger
    }

    private val dataSource = dataSourceProvider.getDataSource(configuration)

    override fun migrate(configureContext: ((MigrationContext) -> Unit)?) {
        // Start the migration
        logger.debug { "<6a5a2585> Apply flyway migrations" }
        val flyway = Flyway.configure()
            .dataSource(dataSource)
            .cleanDisabled(!clean)
            .load()

        if (clean) {
            flyway.clean()
        }

        val migrationContext = migrationContextCreator
            ?.invoke()
            ?.apply { configureContext?.invoke(this) }

        migrationContext?.let { MigrationContext.instance = it }

        try {
            val result = flyway.migrate()
            if (result.success) {
                logger.info { "<28dc3c09> ${result.migrationsExecuted} migrations successfully applied" }
            } else {
                logger.error { "<26163f9d> Can`t apply migrations: \n ${result.warnings.joinToString(", ")}" }
            }
        } finally {
            MigrationContext.instance = null
        }
    }
}