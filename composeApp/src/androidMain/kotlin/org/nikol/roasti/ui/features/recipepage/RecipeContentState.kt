package org.nikol.roasti.ui.features.recipepage

import org.nikol.roasti.ui.features.recipepage.model.RecipeDetailsUiModel

sealed interface RecipeContentState {
    data object Loading: RecipeContentState
    data object Error : RecipeContentState
    data object NotFound: RecipeContentState
    data class Content(val recipe: RecipeDetailsUiModel): RecipeContentState
}
