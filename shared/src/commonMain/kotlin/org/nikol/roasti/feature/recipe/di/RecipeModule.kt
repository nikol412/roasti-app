package org.nikol.roasti.feature.recipe.di

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.nikol.roasti.feature.recipe.data.RecipeRepositoryImpl
import org.nikol.roasti.feature.recipe.data.network.RecipesApiClient
import org.nikol.roasti.feature.recipe.data.network.RecipesApiClientImpl
import org.nikol.roasti.feature.recipe.domain.RecipeRepository
import org.nikol.roasti.feature.recipe.domain.session.BrewingTimer
import org.nikol.roasti.feature.recipe.domain.session.BrewingTimerImpl
import org.nikol.roasti.feature.recipe.presentation.filter.RecipeFilterStore

val recipeModule = module {
    single { RecipesApiClientImpl(get(), get()) } bind RecipesApiClient::class
    single { RecipeRepositoryImpl(get(), get(), get()) } bind RecipeRepository::class
    factoryOf(::BrewingTimerImpl) bind BrewingTimer::class
    factory { RecipeFilterStore() }
}
