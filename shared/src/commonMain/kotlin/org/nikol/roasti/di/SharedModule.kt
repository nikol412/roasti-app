package org.nikol.roasti.di

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.nikol.roasti.recipe.filters.FilterStateHandler
import org.nikol.roasti.recipe.network.RecipesApiClient
import org.nikol.roasti.recipe.network.RecipesApiClientImpl
import org.nikol.roasti.recipe.network.createHttpClient
import org.nikol.roasti.recipe.repository.NetworkRecipeRepository
import org.nikol.roasti.recipe.repository.RecipeRepository
import org.nikol.roasti.recipe.session.BrewingTimer
import org.nikol.roasti.recipe.session.BrewingTimerImpl
import org.nikol.roasti.upload.network.UploadApiClient
import org.nikol.roasti.upload.network.UploadApiClientImpl
import org.nikol.roasti.upload.repository.NetworkUploadRepository
import org.nikol.roasti.upload.repository.UploadRepository


val sharedModule = module {
    single { createHttpClient() }
    single { NetworkRecipeRepository(get()) } bind RecipeRepository::class
    single { UploadApiClientImpl(get()) } bind UploadApiClient::class
    single { NetworkUploadRepository(get()) } bind UploadRepository::class
    single { RecipesApiClientImpl(get()) } bind RecipesApiClient::class
    factoryOf(::BrewingTimerImpl) bind BrewingTimer::class
    factory<FilterStateHandler> { FilterStateHandler() }
}
