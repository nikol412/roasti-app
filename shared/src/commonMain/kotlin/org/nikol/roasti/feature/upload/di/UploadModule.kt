package org.nikol.roasti.feature.upload.di

import org.koin.dsl.bind
import org.koin.dsl.module
import org.nikol.roasti.feature.upload.data.NetworkUploadRepository
import org.nikol.roasti.feature.upload.data.network.UploadApiClient
import org.nikol.roasti.feature.upload.data.network.UploadApiClientImpl
import org.nikol.roasti.feature.upload.domain.UploadRepository

val uploadModule = module {
    single { UploadApiClientImpl(get(), get()) } bind UploadApiClient::class
    single { NetworkUploadRepository(get()) } bind UploadRepository::class
}
