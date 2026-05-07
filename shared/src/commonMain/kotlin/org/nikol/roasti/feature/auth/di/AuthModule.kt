package org.nikol.roasti.feature.auth.di

import org.koin.dsl.module
import org.nikol.roasti.RoastiDatabaseCache
import org.nikol.roasti.UserQueries
import org.nikol.roasti.feature.auth.data.AuthRepositoryImpl
import org.nikol.roasti.feature.auth.data.local.UserCacheDataSource
import org.nikol.roasti.feature.auth.data.network.AuthApiClient
import org.nikol.roasti.feature.auth.data.network.AuthApiClientImpl
import org.nikol.roasti.feature.auth.data.network.ProfileApiClient
import org.nikol.roasti.feature.auth.data.network.ProfileApiClientImpl
import org.nikol.roasti.feature.auth.data.session.SessionStore
import org.nikol.roasti.feature.auth.data.session.TokenRefreshCoordinator
import org.nikol.roasti.feature.auth.domain.repository.AuthRepository
import org.nikol.roasti.core.session.SessionRefresher
import org.nikol.roasti.core.session.SessionRepository

val authModule = module {
    single<UserQueries> { get<RoastiDatabaseCache>().userQueries }
    single { UserCacheDataSource(get()) }
    single<SessionRepository> { SessionStore(get()) }
    single<AuthApiClient> { AuthApiClientImpl(get()) }
    single<ProfileApiClient> { ProfileApiClientImpl(get(), get()) }
    single<SessionRefresher> { TokenRefreshCoordinator(get(), get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get(), get(), get()) }
}
