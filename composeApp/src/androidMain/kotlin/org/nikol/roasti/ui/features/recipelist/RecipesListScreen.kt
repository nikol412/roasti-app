package org.nikol.roasti.ui.features.recipelist

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.nikol.roasti.R
import org.nikol.roasti.domain.recipe.model.BrewMethod
import org.nikol.roasti.domain.recipe.model.Difficulty
import org.nikol.roasti.presentation.recipe.filter.RecipeFilterState
import org.nikol.roasti.ui.features.recipelist.components.BrewMethodFilterRow
import org.nikol.roasti.ui.features.recipelist.components.DifficultyFilterChip
import org.nikol.roasti.ui.features.recipelist.components.RecipeCard
import org.nikol.roasti.ui.features.recipelist.components.RecipeSearchBar
import org.nikol.roasti.ui.features.recipelist.model.RecipeListItemUiModel
import org.nikol.roasti.ui.theme.Spacing
import org.nikol.roasti.ui.uikit.ErrorStub
import org.nikol.roasti.ui.uikit.LoadingStub

private const val RecipeScreenKeyPrefix = "recipe_screen_"

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun RecipesListScreen(
    onRecipeClick: (String) -> Unit = {},
    onCreateClick: () -> Unit = {},
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    contentPadding: PaddingValues,
) {
    val viewModel: RecipesListViewModel = koinViewModel()

    val filtersState by viewModel.filtersState.collectAsStateWithLifecycle()
    val state by viewModel.recipes.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    Box(modifier = Modifier.fillMaxSize()) {
        when (state) {
            RecipesListState.Loading -> LoadingStub(Modifier.align(Alignment.Center))

            RecipesListState.Error -> ErrorStub(
                stringResource(R.string.recipes_load_error),
                modifier = Modifier.padding(contentPadding)
            )

            is RecipesListState.Content -> Content(
                searchQuery = searchQuery,
                filtersState = filtersState,
                state = state as RecipesListState.Content,
                onClick = onRecipeClick,
                onSearch = viewModel::search,
                onLoadMore = viewModel::loadNextPage,
                onRefresh = viewModel::reload,
                onBrewMethodSelected = viewModel::filterByBrewMethod,
                onDifficultySelected = viewModel::filterByDifficulty,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                contentPadding = contentPadding,
                modifier = Modifier.fillMaxSize(),
            )
        }

        FloatingActionButton(
            onClick = onCreateClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = Spacing.lg,
                    bottom = contentPadding.calculateBottomPadding() + Spacing.lg,
                ),
            containerColor = MaterialTheme.colorScheme.primary,
        ) {
            Text("+", style = MaterialTheme.typography.headlineMedium)
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = contentPadding.calculateBottomPadding()),
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun Content(
    searchQuery: String,
    filtersState: RecipeFilterState,
    state: RecipesListState.Content,
    onClick: (String) -> Unit,
    onSearch: (String) -> Unit,
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit,
    onBrewMethodSelected: (BrewMethod) -> Unit,
    onDifficultySelected: (Difficulty?) -> Unit,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(listState, state.isLoadingMore, state.hasMore) {
        snapshotFlow {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val total = listState.layoutInfo.totalItemsCount
            state.hasMore && !state.isLoadingMore && lastVisible >= total - 4
        }
            .distinctUntilChanged()
            .filter { it }
            .collect { onLoadMore() }
    }

    PullToRefreshBox(isRefreshing = state.isRefreshing, onRefresh = onRefresh, modifier) {
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            contentPadding = PaddingValues(
                top = Spacing.sm,
                bottom = contentPadding.calculateBottomPadding() + Spacing.lg,
            ),
        ) {
            stickyHeader(key = "filters") {
                FilterHeader(
                    searchQuery = searchQuery,
                    filtersState = filtersState,
                    onSearch = onSearch,
                    onBrewMethodSelected = onBrewMethodSelected,
                    onDifficultySelected = onDifficultySelected,
                )
            }

            items(state.recipes, key = { it.id }) { recipe ->
                RecipeCard(
                    item = recipe,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg)
                        .then(
                            recipeSharedBoundsModifier(
                                recipeId = recipe.id,
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope,
                            )
                        )
                        .clickable { onClick(recipe.id) },
                )
            }

            if (state.isLoadingMore) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .padding(Spacing.lg),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterHeader(
    searchQuery: String,
    filtersState: RecipeFilterState,
    onSearch: (String) -> Unit,
    onBrewMethodSelected: (BrewMethod) -> Unit,
    onDifficultySelected: (Difficulty?) -> Unit,
) {
    Surface(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            modifier = Modifier.padding(vertical = Spacing.sm),
        ) {
            RecipeSearchBar(
                query = searchQuery,
                onQueryChange = onSearch,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg),
            )
            BrewMethodFilterRow(
                selectedMethod = filtersState.brewMethod,
                onMethodSelected = onBrewMethodSelected,
            )
            Row(modifier = Modifier.padding(horizontal = Spacing.lg)) {
                DifficultyFilterChip(
                    selectedDifficulty = filtersState.difficulty,
                    onDifficultySelected = onDifficultySelected,
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun recipeSharedBoundsModifier(
    recipeId: String,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
): Modifier {
    if (sharedTransitionScope == null || animatedVisibilityScope == null) return Modifier

    return with(sharedTransitionScope) {
        Modifier.sharedBounds(
            sharedContentState = rememberSharedContentState(key = "$RecipeScreenKeyPrefix$recipeId"),
            animatedVisibilityScope = animatedVisibilityScope,
        )
    }
}
