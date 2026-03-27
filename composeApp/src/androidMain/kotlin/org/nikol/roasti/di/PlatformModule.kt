package org.nikol.roasti.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module
import org.nikol.roasti.core.database.SqlDelightDriverFactory
import org.nikol.roasti.core.session.storage.SharedPreferencesTokenStorage
import org.nikol.roasti.core.session.storage.TokenStorage

val platformModule = module {
    single { SharedPreferencesTokenStorage(get()) } bind TokenStorage::class
    single { SqlDelightDriverFactory(androidContext()) }
}
