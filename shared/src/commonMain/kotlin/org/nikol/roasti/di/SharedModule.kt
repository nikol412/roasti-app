package org.nikol.roasti.di

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.nikol.roasti.recipe.network.createHttpClient
import org.nikol.roasti.recipe.repository.NetworkRecipeRepository
import org.nikol.roasti.recipe.repository.RecipeRepository
import org.nikol.roasti.recipe.session.BrewingTimer
import org.nikol.roasti.recipe.session.BrewingTimerImpl

private const val BaseUrl = "http://155.212.158.252:9090"

val sharedModule = module {
    single { createHttpClient() }
    single { NetworkRecipeRepository(get(), BaseUrl) } bind RecipeRepository::class
    factoryOf(::BrewingTimerImpl) bind BrewingTimer::class
}
