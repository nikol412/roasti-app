package org.nikol.roasti.ui.features.recipepage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import org.nikol.roasti.recipe.repository.RecipeRepository


class RecipeContentViewModel(
    private val recipeId: String,
    private val repository: RecipeRepository
) : ViewModel() {
    val state = flow {
        val recipe = repository.getById(recipeId)
        if (recipe != null) {
            emit(RecipeContentState.Content(recipe))
        } else {
            emit(RecipeContentState.NotFound)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RecipeContentState.Loading
    )
}