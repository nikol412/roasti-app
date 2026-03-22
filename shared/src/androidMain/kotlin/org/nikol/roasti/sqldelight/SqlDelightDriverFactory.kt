package org.nikol.roasti.sqldelight

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.nikol.roasti.RoastiDatabaseCache

actual class SqlDelightDriverFactory(private val context: Context) {
    actual fun createDriver() : SqlDriver {
        return AndroidSqliteDriver(RoastiDatabaseCache.Schema, context, "roasti.db")
    }
}