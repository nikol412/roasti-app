package org.nikol.roasti.core.database

import app.cash.sqldelight.db.SqlDriver
import org.nikol.roasti.RoastiDatabaseCache

expect class SqlDelightDriverFactory {
    fun createDriver() : SqlDriver
}

fun createDatabase(driverFactory: SqlDelightDriverFactory): RoastiDatabaseCache {
    val driver = driverFactory.createDriver()
    return RoastiDatabaseCache(driver)
}
