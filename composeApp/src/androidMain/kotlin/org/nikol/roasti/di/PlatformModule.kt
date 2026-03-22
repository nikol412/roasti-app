package org.nikol.roasti.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module
import org.nikol.roasti.auth.data.storage.TokenStorage
import org.nikol.roasti.auth.storage.SharedPreferencesTokenStorage
import org.nikol.roasti.sqldelight.SqlDelightDriverFactory

val platformModule = module {
    single { SharedPreferencesTokenStorage(get()) } bind TokenStorage::class
    single { SqlDelightDriverFactory(androidContext()) }
}
