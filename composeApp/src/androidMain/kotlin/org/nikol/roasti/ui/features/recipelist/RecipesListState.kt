package org.nikol.roasti.ui.features.recipelist

import org.nikol.roasti.ui.features.recipelist.model.RecipeListItemUiModel

sealed interface RecipesListState {
    data object Loading: RecipesListState
    data object Error : RecipesListState
    data class Content(
        val recipes: List<RecipeListItemUiModel>,
        val isRefreshing: Boolean = false, // for pull to refresh, used when we reload our data
        val isLoadingMore: Boolean = false,
        val hasMore: Boolean = true,
        val currentPage: Int = 1,
    ): RecipesListState
}
