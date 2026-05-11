package org.nikol.roasti.core.database

import app.cash.sqldelight.db.SqlDriver
import org.nikol.roasti.Recipe
import org.nikol.roasti.RoastiDatabaseCache

expect class SqlDelightDriverFactory {
    fun createDriver() : SqlDriver
}

fun createDatabase(driverFactory: SqlDelightDriverFactory): RoastiDatabaseCache {
    val driver = driverFactory.createDriver()
    return RoastiDatabaseCache(
        driver = driver,
        RecipeAdapter = Recipe.Adapter(
            brew_methodAdapter = brewMethodColumnAdapter,
            difficultyAdapter = difficultyColumnAdapter,
            roast_levelAdapter = roastLevelColumnAdapter,
        ),
    )
}
