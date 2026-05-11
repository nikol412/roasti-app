package org.nikol.roasti.feature.likes.di

import org.koin.dsl.bind
import org.koin.dsl.module
import org.nikol.roasti.feature.likes.data.LikesApiClient
import org.nikol.roasti.feature.likes.data.LikesApiClientImpl

val likesModule = module {
    single { LikesApiClientImpl(get(), get()) } bind LikesApiClient::class
}
