package org.nikol.roasti.ui.features.recipelist

import org.nikol.roasti.recipe.model.Recipe

sealed interface RecipesListState {
    data object Loading: RecipesListState
    data object Error : RecipesListState
    data class Content(val recipes: List<Recipe>): RecipesListState
}
