package org.nikol.roasti.ui.features.recipelist

import org.nikol.roasti.recipe.model.Recipe

sealed interface RecipesListState {
    data object Loading: RecipesListState
    data object Error : RecipesListState
    data class Content(
        val recipes: List<Recipe>,
        val isRefreshing: Boolean = false, // for pull to refresh, used when we reload our data
        val isLoadingMore: Boolean = false,
        val hasMore: Boolean = true,
        val currentPage: Int = 1,
    ): RecipesListState
}
