package org.nikol.roasti.ui.features.recipepage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nikol.roasti.feature.likes.domain.LikesRepository
import org.nikol.roasti.feature.recipe.domain.RecipeRepository
import org.nikol.roasti.ui.features.recipepage.mapper.toUiModel


class RecipeContentViewModel(
    private val recipeId: String,
    private val repository: RecipeRepository,
    private val likesRepository: LikesRepository,
) : ViewModel() {

    private val _eventFlow: MutableSharedFlow<RecipeContentEvent> = MutableSharedFlow()
    val eventFlow: SharedFlow<RecipeContentEvent> = _eventFlow.asSharedFlow()

    private val _state = MutableStateFlow<RecipeContentState>(RecipeContentState.Loading)
    val state: StateFlow<RecipeContentState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.getByIdFlow(recipeId).collectLatest { recipe ->
                    _state.value = RecipeContentState.Content(recipe.toUiModel())
                }
            } catch (_: Exception) {
                _state.value = RecipeContentState.Error
            }
        }
    }

    fun toggleLike() {
        val content = _state.value as? RecipeContentState.Content ?: return
        val recipe = content.recipe
        val optimisticLiked = !recipe.isLiked
        val optimisticCount = recipe.likesCount + if (optimisticLiked) 1 else -1

        _state.update {
            content.copy(recipe = recipe.copy(isLiked = optimisticLiked, likesCount = optimisticCount))
        }

        viewModelScope.launch {
            likesRepository.toggleLikeOnRecipe(recipeId)
                .onFailure {
                    _state.update {
                        content.copy(recipe = recipe.copy(isLiked = recipe.isLiked, likesCount = recipe.likesCount))
                    }
                }
        }
    }

    fun onRemoveRecipe() {
        viewModelScope.launch {
            repository.removeRecipe(recipeId).onSuccess {
                _eventFlow.emit(RecipeContentEvent.NavigateBack)
            }
        }
    }
}
