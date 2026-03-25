package org.nikol.roasti.ui.features.recipelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nikol.roasti.feature.likes.domain.LikesRepository
import org.nikol.roasti.feature.recipe.domain.RecipeRepository
import org.nikol.roasti.feature.recipe.domain.model.BrewMethod
import org.nikol.roasti.feature.recipe.domain.model.Difficulty
import org.nikol.roasti.feature.recipe.domain.model.RoastLevel
import org.nikol.roasti.feature.recipe.presentation.filter.RecipeFilterState
import org.nikol.roasti.feature.recipe.presentation.filter.RecipeFilterStore
import org.nikol.roasti.ui.features.recipelist.mapper.toUiModel
import org.nikol.roasti.ui.features.recipelist.model.RecipeListItemUiModel

private const val FirstPage = 1

class RecipesListViewModel(
    private val recipeRepository: RecipeRepository,
    private val likesRepository: LikesRepository,
    private val filterStore: RecipeFilterStore,
) : ViewModel() {

    val filtersState: StateFlow<RecipeFilterState> = filterStore.state

    private val _baseState = MutableStateFlow<RecipesListState>(RecipesListState.Loading)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val favoriteRecipesState: StateFlow<FavoritesRecipesState> = flow {
        val items = likesRepository.getUserLikedRecipes().getOrNull()
        if (items != null) {
            emit(FavoritesRecipesState.Content(items.items.map { it.recipe.toUiModel() }))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FavoritesRecipesState.Empty)

    val recipes: StateFlow<RecipesListState> = combine(_baseState, _searchQuery) { state, query ->
        if (state is RecipesListState.Content && query.isNotBlank()) {
            state.copy(
                recipes = state.recipes.filter { recipe ->
                    recipe.title.contains(query, ignoreCase = true) ||
                            recipe.description.contains(query, ignoreCase = true)
                }
            )
        } else {
            state
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecipesListState.Loading)

    init {
        viewModelScope.launch {
            filtersState.collectLatest { filters ->
                loadPage(page = FirstPage, filters = filters)
            }
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun loadNextPage() {
        val state = _baseState.value as? RecipesListState.Content ?: return
        if (state.isLoadingMore || !state.hasMore) return

        val nextPage = state.nextPage ?: return
        _baseState.value = state.copy(isLoadingMore = true)
        viewModelScope.launch {
            val result = recipeRepository.getRecipes(
                brewMethod = filtersState.value.brewMethod,
                difficulty = filtersState.value.difficulty,
                roastLevel = filtersState.value.roastLevel,
                page = nextPage,
            ).getOrNull()

            val current = _baseState.value as? RecipesListState.Content ?: return@launch
            _baseState.value = if (result != null) {
                val mergedRecipes =
                    (current.recipes + result.items.map { it.toUiModel() }).distinctBy { it.id }
                val pageAdvanced = result.currentPage >= nextPage
                val newItemsAdded = mergedRecipes.size > current.recipes.size
                val hasProgress = pageAdvanced || newItemsAdded

                if (!hasProgress) {
                    current.copy(
                        isLoadingMore = false,
                        hasMore = result.hasNextPage(),
                        nextPage = result.nextPageOrNull(),
                    )
                } else {
                    current.copy(
                        recipes = mergedRecipes,
                        isLoadingMore = false,
                        hasMore = result.hasNextPage(),
                        currentPage = maxOf(current.currentPage, result.currentPage),
                        nextPage = result.nextPageOrNull(),
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

    fun reload() {
        viewModelScope.launch {
            val isContentState = _baseState.value is RecipesListState.Content
            if (isContentState) {
                _baseState.update {
                    if (it is RecipesListState.Content) it.copy(isRefreshing = true)
                    else RecipesListState.Loading
                }
                loadPage(page = FirstPage, filters = filtersState.value)
            }
        }
    }

    fun filterByBrewMethod(method: BrewMethod) {
        val actual = method.takeIf { it != BrewMethod.NONE }
        filterStore.applyFilter(actual, actual != null)
    }

    fun filterByDifficulty(difficulty: Difficulty?) {
        filterStore.applyFilter(difficulty, difficulty != null)
    }

    fun filterByRoastLevel(roastLevel: RoastLevel?) {
        filterStore.applyFilter(roastLevel, roastLevel != null)
    }

    fun likeRecipe(recipe: RecipeListItemUiModel) {
        val optimisticLiked = !recipe.isLiked
        val optimisticCount = recipe.likesCount + if (optimisticLiked) 1 else -1

        updateRecipeLikeState(recipe.id, optimisticLiked, optimisticCount)

        viewModelScope.launch {
            likesRepository.toggleLikeOnRecipe(recipe.id)
                .onFailure {
                    updateRecipeLikeState(recipe.id, recipe.isLiked, recipe.likesCount)
                }
        }
    }

    private fun updateRecipeLikeState(recipeId: String, isLiked: Boolean, likesCount: Int) {
        _baseState.update { state ->
            if (state is RecipesListState.Content) {
                state.copy(
                    recipes = state.recipes.map { item ->
                        if (item.id == recipeId) item.copy(
                            isLiked = isLiked,
                            likesCount = likesCount
                        )
                        else item
                    }
                )
            } else state
        }
    }

    private suspend fun loadPage(page: Int, filters: RecipeFilterState) {
        val result = recipeRepository.getRecipes(
            brewMethod = filters.brewMethod,
            difficulty = filters.difficulty,
            roastLevel = filters.roastLevel,
            page = page,
        ).getOrNull()

        _baseState.value = if (result != null) {
            RecipesListState.Content(
                recipes = result.items.map { it.toUiModel() },
                hasMore = result.hasNextPage(),
                currentPage = result.currentPage,
                nextPage = result.nextPageOrNull(),
            )
        } else {
            RecipesListState.Error
        }
    }
}

private fun org.nikol.roasti.feature.recipe.domain.model.RecipesPage.hasNextPage(): Boolean =
    currentPage < lastPage

private fun org.nikol.roasti.feature.recipe.domain.model.RecipesPage.nextPageOrNull(): Int? =
    nextPage.takeIf { hasNextPage() }
