package org.nikol.roasti.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.nikol.roasti.ui.features.createrecipe.CreateRecipeViewModel
import org.nikol.roasti.ui.features.recipelist.RecipesListViewModel
import org.nikol.roasti.ui.features.recipepage.RecipeContentViewModel
import org.nikol.roasti.ui.features.recipesteps.RecipeStepsViewModel

val viewModelsModule = module {
    viewModel { RecipesListViewModel(get(), get()) }
    viewModel { params -> RecipeContentViewModel(params.get(), get()) }
    viewModel { params -> RecipeStepsViewModel(params.get(), params.get(), get(), get()) }
    viewModel { CreateRecipeViewModel(get(), get()) }
}
