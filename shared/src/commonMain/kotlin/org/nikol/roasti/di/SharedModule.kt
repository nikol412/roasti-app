package org.nikol.roasti.di

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.nikol.roasti.recipe.network.RecipesApiClient
import org.nikol.roasti.recipe.network.RecipesApiClientImpl
import org.nikol.roasti.recipe.network.createHttpClient
import org.nikol.roasti.recipe.repository.NetworkRecipeRepository
import org.nikol.roasti.recipe.repository.RecipeRepository
import org.nikol.roasti.recipe.session.BrewingTimer
import org.nikol.roasti.recipe.session.BrewingTimerImpl


val sharedModule = module {
    single { createHttpClient() }
    single { NetworkRecipeRepository(get()) } bind RecipeRepository::class
    single { RecipesApiClientImpl(get()) } bind RecipesApiClient::class
    factoryOf(::BrewingTimerImpl) bind BrewingTimer::class
}
