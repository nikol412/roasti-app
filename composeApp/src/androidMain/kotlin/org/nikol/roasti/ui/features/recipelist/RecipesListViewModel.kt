package org.nikol.roasti.ui.features.recipelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.nikol.roasti.recipe.filters.FilterStateHandler
import org.nikol.roasti.recipe.filters.FiltersState
import org.nikol.roasti.recipe.model.BrewMethod
import org.nikol.roasti.recipe.model.Difficulty
import org.nikol.roasti.recipe.repository.RecipeRepository

private const val FirstPage = 1

class RecipesListViewModel(
    private val recipeRepository: RecipeRepository,
    private val filtersStateHandler: FilterStateHandler,
) : ViewModel() {
    val filtersState: StateFlow<FiltersState> = filtersStateHandler.state

    private val _recipes = MutableStateFlow<RecipesListState>(RecipesListState.Loading)
    val recipes: StateFlow<RecipesListState> = _recipes

    init {
        viewModelScope.launch {
            filtersState.collectLatest { filters ->
                _recipes.value = RecipesListState.Loading
                loadPage(page = FirstPage, filters = filters)
            }
        }
    }

    fun loadNextPage() {
        val state = _recipes.value as? RecipesListState.Content ?: return
        if (state.isLoadingMore || !state.hasMore) return

        val nextPage = state.currentPage + 1
        _recipes.value = state.copy(isLoadingMore = true)
        viewModelScope.launch {
            val result = recipeRepository.getRecipes(
                brewMethod = filtersState.value.brewMethod,
                difficulty = filtersState.value.difficulty,
                page = nextPage,
            ).getOrNull()

            val current = _recipes.value as? RecipesListState.Content ?: return@launch
            _recipes.value = if (result != null) {
                val mergedRecipes = (current.recipes + result.items).distinctBy { it.id }
                val pageAdvanced = result.page >= nextPage
                val newItemsAdded = mergedRecipes.size > current.recipes.size
                val hasProgress = pageAdvanced || newItemsAdded

                if (!hasProgress) {
                    current.copy(
                        isLoadingMore = false,
                        hasMore = false,
                    )
                } else {
                    val hasMore = mergedRecipes.size < result.totalCount
                    current.copy(
                        recipes = mergedRecipes,
                        isLoadingMore = false,
                        hasMore = hasMore,
                        currentPage = maxOf(current.currentPage, result.page),
                    )
                }
            } else {
                current.copy(
                    isLoadingMore = false,
                    hasMore = false,
                )
            }
        }
    }

    fun filterByBrewMethod(method: BrewMethod?) {
        filtersStateHandler.applyFilter(method, method != null)
    }

    fun filterByDifficulty(difficulty: Difficulty?, apply: Boolean = true) {
        filtersStateHandler.applyFilter(difficulty, difficulty != null)
    }

    private suspend fun loadPage(page: Int, filters: FiltersState) {
        val result = recipeRepository.getRecipes(
            brewMethod = filters.brewMethod,
            difficulty = filters.difficulty,
            page = page,
        ).getOrNull()

        _recipes.value = if (result != null) {
            val hasMore = result.items.isNotEmpty() && result.page * result.limit < result.totalCount
            RecipesListState.Content(
                recipes = result.items,
                hasMore = hasMore,
                currentPage = result.page,
            )
        } else {
            RecipesListState.Error
        }
    }
}
