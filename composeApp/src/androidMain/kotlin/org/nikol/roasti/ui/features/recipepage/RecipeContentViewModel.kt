package org.nikol.roasti.ui.features.recipepage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.nikol.roasti.domain.recipe.RecipeRepository
import org.nikol.roasti.ui.features.recipepage.mapper.toUiModel


class RecipeContentViewModel(
    private val recipeId: String,
    private val repository: RecipeRepository
) : ViewModel() {

    private val _eventFlow: MutableSharedFlow<RecipeContentEvent> = MutableSharedFlow()
    val eventFlow: SharedFlow<RecipeContentEvent> = _eventFlow.asSharedFlow()

    val state: StateFlow<RecipeContentState> = flow {
        val recipe = repository.getById(recipeId).getOrNull()?.toUiModel()
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

    fun onRemoveRecipe() {
        viewModelScope.launch {
            repository.removeRecipe(recipeId).onSuccess {
                _eventFlow.emit(RecipeContentEvent.NavigateBack)
            }
        }
    }
}
