package org.nikol.roasti.core.di

import org.koin.dsl.module
import org.nikol.roasti.core.network.AuthorizedRequestExecutor
import org.nikol.roasti.core.network.createHttpClient
import org.nikol.roasti.core.session.SessionRepository

val coreNetworkModule = module {
    single {
        val sessionRepository: SessionRepository = get()
        createHttpClient(
            accessTokenProvider = { sessionRepository.currentSession()?.accessToken }
        )
    }
    single { AuthorizedRequestExecutor(get(), get()) }
}
