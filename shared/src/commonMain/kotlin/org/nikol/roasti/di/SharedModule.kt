package org.nikol.roasti.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.nikol.roasti.recipe.repository.InMemoryRecipeRepository
import org.nikol.roasti.recipe.repository.RecipeRepository
import org.nikol.roasti.recipe.session.BrewingTimer
import org.nikol.roasti.recipe.session.BrewingTimerImpl

val sharedModule = module {
    singleOf(::InMemoryRecipeRepository) bind RecipeRepository::class
    factoryOf(::BrewingTimerImpl) bind BrewingTimer::class
}
