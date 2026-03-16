package org.nikol.roasti.presentation.recipe.filter

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.nikol.roasti.domain.recipe.model.BrewMethod
import org.nikol.roasti.domain.recipe.model.Difficulty

data class RecipeFilterState(
    val brewMethod: BrewMethod? = null,
    val difficulty: Difficulty? = null,
)

class RecipeFilterStore {

    private val _state: MutableStateFlow<RecipeFilterState> = MutableStateFlow(RecipeFilterState())
    val state: StateFlow<RecipeFilterState> = _state.asStateFlow()

    fun applyFilter(difficulty: Difficulty?, enabled: Boolean = true) {
        val newValue = difficulty.takeIf { enabled }
        _state.update { it.copy(difficulty = newValue) }
    }

    fun applyFilter(brewMethod: BrewMethod?, enabled: Boolean = true) {
        val newValue = brewMethod.takeIf { enabled }
        _state.update { it.copy(brewMethod = newValue) }
    }
}
