package org.nikol.roasti.di
import app.cash.sqldelight.db.SqlDriver
import org.koin.dsl.module
import org.nikol.roasti.RoastiDatabaseCache
import org.nikol.roasti.UserQueries
import org.nikol.roasti.auth.data.local.UserCacheDataSource
import org.nikol.roasti.sqldelight.SqlDelightDriverFactory
import org.nikol.roasti.sqldelight.createDatabase

val dbSharedModule = module {
    single<RoastiDatabaseCache> { createDatabase(get()) }
    single<UserQueries> { get<RoastiDatabaseCache>().userQueries }
    single { UserCacheDataSource(get()) }
}