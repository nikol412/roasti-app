package org.nikol.roasti.ui.features.recipelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import org.nikol.roasti.recipe.repository.RecipeRepository

class RecipesListViewModel(
    private val recipeRepository: RecipeRepository
) : ViewModel() {
    val recipes: StateFlow<RecipesListState> = flow<RecipesListState> {
        val list = recipeRepository.getAll()
        emit(RecipesListState.Content(list))
    }
        .catch { emit(RecipesListState.Error) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecipesListState.Loading)
}
