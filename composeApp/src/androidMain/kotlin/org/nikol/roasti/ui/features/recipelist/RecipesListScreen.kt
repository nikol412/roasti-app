package org.nikol.roasti.ui.features.recipelist

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import org.koin.compose.viewmodel.koinViewModel
import org.nikol.roasti.R
import org.nikol.roasti.feature.recipe.domain.model.BrewMethod
import org.nikol.roasti.feature.recipe.domain.model.Difficulty
import org.nikol.roasti.feature.recipe.domain.model.RoastLevel
import org.nikol.roasti.feature.recipe.presentation.filter.RecipeFilterState
import org.nikol.roasti.ui.features.recipelist.components.BrewMethodFilterChip
import org.nikol.roasti.ui.features.recipelist.components.DifficultyFilterChip
import org.nikol.roasti.ui.features.recipelist.components.RecipeCard
import org.nikol.roasti.ui.features.recipelist.components.RecipeCompactCard
import org.nikol.roasti.ui.features.recipelist.components.RecipeSearchBar
import org.nikol.roasti.ui.features.recipelist.components.RoastLevelFilterChip
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
    val favoriteState by viewModel.favoriteRecipesState.collectAsStateWithLifecycle()
    val state by viewModel.recipes.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.reload()
        }
    }

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
                favoritesState = favoriteState,
                onClick = onRecipeClick,
                onLikeClick = viewModel::likeRecipe,
                onSearch = viewModel::search,
                onLoadMore = viewModel::loadNextPage,
                onRefresh = viewModel::reload,
                onBrewMethodSelected = viewModel::filterByBrewMethod,
                onDifficultySelected = viewModel::filterByDifficulty,
                onRoastLevelSelected = viewModel::filterByRoastLevel,
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
    favoritesState: FavoritesRecipesState,
    state: RecipesListState.Content,
    onClick: (String) -> Unit,
    onLikeClick: (RecipeListItemUiModel) -> Unit,
    onSearch: (String) -> Unit,
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit,
    onBrewMethodSelected: (BrewMethod) -> Unit,
    onDifficultySelected: (Difficulty?) -> Unit,
    onRoastLevelSelected: (RoastLevel?) -> Unit,
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
                    onRoastLevelSelected = onRoastLevelSelected,
                    modifier = Modifier.animateItem(),
                )
            }


            item("FAVORITES_KEY") {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    Text(
                        stringResource(R.string.recipe_list_favorite_section_title),
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        modifier = Modifier.padding(start = Spacing.lg)
                    )
                    LazyRow(
                        state = rememberLazyListState(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        contentPadding = PaddingValues(horizontal = Spacing.lg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (favoritesState is FavoritesRecipesState.Content) {
                            items(favoritesState.items) { item ->
                                RecipeCompactCard(
                                    item = item,
                                    modifier = Modifier.width(200.dp),
                                    onClick = { onClick(item.id) },
                                    onLikeClick = { onLikeClick(item) })
                            }
                        }
                    }
                }
            }


            item {
                Text(
                    stringResource(R.string.recipe_list_all_section_title),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    modifier = Modifier.padding(start = Spacing.lg)
                )
            }

            items(state.recipes, key = { it.id }) { recipe ->
                RecipeCard(
                    item = recipe,
                    onLikeClick = { onLikeClick(recipe) },
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
                        .clickable { onClick(recipe.id) }
                        .animateItem(),
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
                            modifier = Modifier
                                .size(32.dp)
                                .animateItem(),
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
    onRoastLevelSelected: (RoastLevel?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxWidth()) {
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.lg),
            ) {
                BrewMethodFilterChip(
                    selectedMethod = filtersState.brewMethod,
                    onMethodSelected = onBrewMethodSelected,
                )
                DifficultyFilterChip(
                    selectedDifficulty = filtersState.difficulty,
                    onDifficultySelected = onDifficultySelected,
                )
                RoastLevelFilterChip(
                    selectedRoastLevel = filtersState.roastLevel,
                    onRoastLevelSelected = onRoastLevelSelected,
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
