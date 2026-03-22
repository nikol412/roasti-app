package org.nikol.roasti.sqldelight

import app.cash.sqldelight.db.SqlDriver
import org.nikol.roasti.RoastiDatabaseCache

expect class SqlDelightDriverFactory {
    fun createDriver() : SqlDriver
}

fun createDatabase(driverFactory: SqlDelightDriverFactory): RoastiDatabaseCache {
    val driver = driverFactory.createDriver()
    return RoastiDatabaseCache(driver)
}