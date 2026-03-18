package org.nikol.roasti.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.nikol.roasti.navigation.AppNavigationViewModel
import org.nikol.roasti.ui.features.auth.login.LoginViewModel
import org.nikol.roasti.ui.features.auth.register.RegisterViewModel
import org.nikol.roasti.ui.features.createrecipe.CreateRecipeViewModel
import org.nikol.roasti.ui.features.editrecipe.EditRecipeViewModel
import org.nikol.roasti.ui.features.profile.ProfileViewModel
import org.nikol.roasti.ui.features.recipelist.RecipesListViewModel
import org.nikol.roasti.ui.features.recipepage.RecipeContentViewModel
import org.nikol.roasti.ui.features.recipesteps.RecipeStepsViewModel

val viewModelsModule = module {
    viewModel { AppNavigationViewModel(get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { ProfileViewModel(get(), get()) }
    viewModel { RecipesListViewModel(get(), get()) }
    viewModel { params -> RecipeContentViewModel(params.get(), get()) }
    viewModel { params -> RecipeStepsViewModel(params.get(), params.get(), get(), get()) }
    viewModel { CreateRecipeViewModel(get(), get()) }
    viewModel { params -> EditRecipeViewModel(params.get(), get(), get()) }
}
