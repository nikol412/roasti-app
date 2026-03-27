package org.nikol.roasti.core.di

import org.koin.dsl.module
import org.nikol.roasti.RoastiDatabaseCache
import org.nikol.roasti.core.database.createDatabase

val coreDatabaseModule = module {
    single<RoastiDatabaseCache> { createDatabase(get()) }
}
