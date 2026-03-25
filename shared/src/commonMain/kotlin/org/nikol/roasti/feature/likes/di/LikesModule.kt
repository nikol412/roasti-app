package org.nikol.roasti.feature.likes.di

import org.koin.dsl.module
import org.nikol.roasti.feature.likes.data.LikesApiClient
import org.nikol.roasti.feature.likes.data.LikesRepositoryImpl
import org.nikol.roasti.feature.likes.domain.LikesRepository

val likesModule = module {
    single { LikesApiClient(get(), get()) }
    single<LikesRepository> { LikesRepositoryImpl(get(), get()) }
}
