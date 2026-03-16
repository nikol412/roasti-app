package org.nikol.roasti.ui.features.recipepage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import org.nikol.roasti.domain.recipe.RecipeRepository
import org.nikol.roasti.ui.features.recipepage.mapper.toUiModel


class RecipeContentViewModel(
    private val recipeId: String,
    private val repository: RecipeRepository
) : ViewModel() {
    val state: StateFlow<RecipeContentState> = flow {
        val recipe = repository.getById(recipeId)?.toUiModel()
        if (recipe != null) {
            emit(RecipeContentState.Content(recipe))
        } else {
            emit(RecipeContentState.NotFound)
        }
    }
        .catch { emit(RecipeContentState.Error) }
        .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RecipeContentState.Loading
    )
}
