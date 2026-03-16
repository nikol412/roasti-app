package org.nikol.roasti.ui.features.recipepage

import org.nikol.roasti.domain.recipe.Recipe

sealed interface RecipeContentState {
    data object Loading: RecipeContentState
    data object Error : RecipeContentState
    data object NotFound: RecipeContentState
    data class Content(val recipe: Recipe): RecipeContentState
}
