package org.nikol.roasti.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import org.nikol.roasti.recipe.repository.InMemoryRecipeRepository
import org.nikol.roasti.recipe.repository.RecipeRepository
import org.nikol.roasti.recipe.session.BrewingSessionManager
import org.nikol.roasti.ui.features.recipelist.RecipesListViewModel
import org.nikol.roasti.ui.features.recipepage.RecipeContentViewModel

val viewModelsModule = module {
    viewModel { RecipesListViewModel(get()) }
    viewModel { RecipeContentViewModel(get(), get()) }
}