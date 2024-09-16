package dev.limebeck.openconf.db

interface MigrationContext {
    companion object {
        var instance: MigrationContext? = null
    }
}

interface MigrationService {
    fun migrate(configureContext: ((MigrationContext) -> Unit)? = null)
}
