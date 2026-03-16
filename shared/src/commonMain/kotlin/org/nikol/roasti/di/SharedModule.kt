package org.nikol.roasti.di

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.nikol.roasti.data.network.createHttpClient
import org.nikol.roasti.data.recipe.NetworkRecipeRepository
import org.nikol.roasti.data.recipe.network.RecipesApiClient
import org.nikol.roasti.data.recipe.network.RecipesApiClientImpl
import org.nikol.roasti.data.upload.NetworkUploadRepository
import org.nikol.roasti.data.upload.network.UploadApiClient
import org.nikol.roasti.data.upload.network.UploadApiClientImpl
import org.nikol.roasti.domain.recipe.RecipeRepository
import org.nikol.roasti.domain.recipe.filters.FilterStateHandler
import org.nikol.roasti.domain.recipe.session.BrewingTimer
import org.nikol.roasti.domain.recipe.session.BrewingTimerImpl
import org.nikol.roasti.domain.upload.UploadRepository


val sharedModule = module {
    single { createHttpClient() }
    single { NetworkRecipeRepository(get()) } bind RecipeRepository::class
    single { UploadApiClientImpl(get()) } bind UploadApiClient::class
    single { NetworkUploadRepository(get()) } bind UploadRepository::class
    single { RecipesApiClientImpl(get()) } bind RecipesApiClient::class
    factoryOf(::BrewingTimerImpl) bind BrewingTimer::class
    factory<FilterStateHandler> { FilterStateHandler() }
}
