package org.nikol.roasti.di

import org.koin.dsl.bind
import org.koin.dsl.module
import org.nikol.roasti.auth.data.storage.TokenStorage
import org.nikol.roasti.auth.storage.SharedPreferencesTokenStorage

val platformModule = module {
    single { SharedPreferencesTokenStorage(get()) } bind TokenStorage::class
}
