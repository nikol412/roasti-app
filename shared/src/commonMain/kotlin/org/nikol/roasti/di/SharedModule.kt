package org.nikol.roasti.di

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.nikol.roasti.auth.data.NetworkAuthRepository
import org.nikol.roasti.auth.data.network.AuthApiClient
import org.nikol.roasti.auth.data.network.AuthApiClientImpl
import org.nikol.roasti.auth.data.network.ProfileApiClient
import org.nikol.roasti.auth.data.network.ProfileApiClientImpl
import org.nikol.roasti.auth.data.session.SessionStore
import org.nikol.roasti.auth.data.session.TokenRefreshCoordinator
import org.nikol.roasti.auth.domain.repository.AuthRepository
import org.nikol.roasti.auth.domain.repository.SessionRepository
import org.nikol.roasti.auth.domain.session.SessionRefresher
import org.nikol.roasti.data.network.createHttpClient
import org.nikol.roasti.data.network.AuthorizedRequestExecutor
import org.nikol.roasti.data.recipe.NetworkRecipeRepository
import org.nikol.roasti.data.recipe.network.RecipesApiClient
import org.nikol.roasti.data.recipe.network.RecipesApiClientImpl
import org.nikol.roasti.data.upload.NetworkUploadRepository
import org.nikol.roasti.data.upload.network.UploadApiClient
import org.nikol.roasti.data.upload.network.UploadApiClientImpl
import org.nikol.roasti.domain.recipe.RecipeRepository
import org.nikol.roasti.domain.recipe.session.BrewingTimer
import org.nikol.roasti.domain.recipe.session.BrewingTimerImpl
import org.nikol.roasti.domain.upload.UploadRepository
import org.nikol.roasti.presentation.recipe.filter.RecipeFilterStore


val sharedModule = module {
    single<SessionRepository> { SessionStore(get()) }
    single {
        val sessionRepository: SessionRepository = get()
        createHttpClient(
            accessTokenProvider = { sessionRepository.currentSession()?.accessToken }
        )
    }
    single<AuthApiClient> { AuthApiClientImpl(get()) }
    single<SessionRefresher> { TokenRefreshCoordinator(get(), get()) }
    single { AuthorizedRequestExecutor(get(), get()) }
    single<ProfileApiClient> { ProfileApiClientImpl(get(), get()) }
    single<AuthRepository> { NetworkAuthRepository(get(), get(), get()) }
    single { NetworkRecipeRepository(get()) } bind RecipeRepository::class
    single { UploadApiClientImpl(get(), get()) } bind UploadApiClient::class
    single { NetworkUploadRepository(get()) } bind UploadRepository::class
    single { RecipesApiClientImpl(get(), get()) } bind RecipesApiClient::class
    factoryOf(::BrewingTimerImpl) bind BrewingTimer::class
    factory<RecipeFilterStore> { RecipeFilterStore() }
}
