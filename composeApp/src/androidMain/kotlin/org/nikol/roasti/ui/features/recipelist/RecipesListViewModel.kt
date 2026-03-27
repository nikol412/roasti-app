package org.nikol.roasti.ui.features.recipelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.nikol.roasti.feature.recipe.data.paging.PagingRecipeRepository
import org.nikol.roasti.feature.recipe.data.paging.RecipesPagingQuery
import org.nikol.roasti.feature.recipe.domain.model.BrewMethod
import org.nikol.roasti.feature.recipe.domain.model.Difficulty
import org.nikol.roasti.feature.recipe.domain.model.RoastLevel
import org.nikol.roasti.feature.recipe.presentation.filter.RecipeFilterState
import org.nikol.roasti.feature.recipe.presentation.filter.RecipeFilterStore
import org.nikol.roasti.ui.features.recipelist.mapper.toUiModel
import org.nikol.roasti.ui.features.recipelist.model.RecipeListItemUiModel

private const val SearchQueryDebounceMillis = 300L

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class RecipesListViewModel(
    private val filterStore: RecipeFilterStore,
    private val pagingRepository: PagingRecipeRepository,
) : ViewModel() {
    val hasCachedRecipes: StateFlow<Boolean> =
        pagingRepository.observeHasCachedRecipes()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = false,
            )

    val filtersState: Flow<RecipeFilterState> = filterStore.state

    private val searchQueryMutable = MutableStateFlow("")
    val searchQuery: Flow<String> = searchQueryMutable.asStateFlow()

    private val manualRefreshMutable = MutableStateFlow(false)
    val isManualRefresh: StateFlow<Boolean> = manualRefreshMutable.asStateFlow()

    private val recipesQuery: Flow<RecipesPagingQuery> =
        combine(
            searchQueryMutable
                .debounce { query ->
                    if (query.isBlank()) {
                        0L
                    } else {
                        SearchQueryDebounceMillis
                    }
                }
                .map(String::trim)
                .distinctUntilChanged(),
            filterStore.state,
        ) { query, filters ->
            RecipesPagingQuery(
                query = query,
                brewMethod = filters.brewMethod,
                difficulty = filters.difficulty,
                roastLevel = filters.roastLevel,
            )
        }.distinctUntilChanged()

    val isDefaultFeedMode: StateFlow<Boolean> =
        recipesQuery
            .map { query -> query.isDefaultFeed }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = true,
            )

    val pagingRecipesState: Flow<PagingData<RecipeListItemUiModel>> =
        recipesQuery
            .flatMapLatest { query ->
                if (query.isDefaultFeed) {
                    pagingRepository.getOfflineFirstAllRecipesPager()
                        .map { pagingData -> pagingData.map { it.toUiModel() } }
                } else {
                    pagingRepository.getRemoteSearchPager(query)
                        .map { pagingData -> pagingData.map { it.toUiModel() } }
                }
            }
            .cachedIn(viewModelScope)

    val pagingFavoritesState: Flow<PagingData<RecipeListItemUiModel>> =
        pagingRepository.getFavoritesPager()
            .map { pagingData -> pagingData.map { it.toUiModel() } }
            .cachedIn(viewModelScope)

    fun search(query: String) {
        searchQueryMutable.value = query
    }

    fun filterByBrewMethod(method: BrewMethod) {
        filterStore.applyFilter(method.takeIf { it != BrewMethod.NONE })
    }

    fun filterByDifficulty(difficulty: Difficulty?) {
        filterStore.applyFilter(difficulty)
    }

    fun filterByRoastLevel(roastLevel: RoastLevel?) {
        filterStore.applyFilter(roastLevel)
    }

    fun likeRecipe(recipe: RecipeListItemUiModel) {
        viewModelScope.launch {
            pagingRepository.toggleLike(recipe.id)
        }
    }

    fun startManualRefresh() {
        manualRefreshMutable.value = true
    }

    fun finishManualRefresh() {
        manualRefreshMutable.value = false
    }
}
