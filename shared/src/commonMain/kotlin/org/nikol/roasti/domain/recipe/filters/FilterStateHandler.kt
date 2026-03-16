package org.nikol.roasti.domain.recipe.filters

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.nikol.roasti.domain.recipe.BrewMethod
import org.nikol.roasti.domain.recipe.Difficulty

data class FiltersState(
    val brewMethod: BrewMethod? = null,
    val difficulty: Difficulty? = null,
)

class FilterStateHandler() {

    private val _state: MutableStateFlow<FiltersState> = MutableStateFlow(FiltersState())
    val state: StateFlow<FiltersState> = _state.asStateFlow()

    fun applyFilter(difficulty: Difficulty?, enabled: Boolean = true) {
        val newValue = difficulty.takeIf { enabled }
        _state.update { it.copy(difficulty = newValue) }
    }

    fun applyFilter(brewMethod: BrewMethod?, enabled: Boolean = true) {
        val newValue = brewMethod.takeIf { enabled }
        _state.update { it.copy(brewMethod = newValue) }
    }
}
