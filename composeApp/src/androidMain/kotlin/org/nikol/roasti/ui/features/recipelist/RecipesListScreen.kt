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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
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
import org.nikol.roasti.ui.theme.RoastiTheme
import org.nikol.roasti.ui.theme.Spacing
import org.nikol.roasti.ui.uikit.ErrorStub
import org.nikol.roasti.ui.uikit.LoadingStub

private const val FavoritesSectionKey = "favorite_recipes_section"
private const val RecipeScreenKeyPrefix = "recipe_screen_"
private val FavoriteCardWidth = 200.dp
private val RecipeCardHeight = 130.dp
private val FavoriteCardHeight = 150.dp

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

    val filtersState by viewModel.filtersState.collectAsStateWithLifecycle(RecipeFilterState())
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle("")
    val hasCachedRecipes by viewModel.hasCachedRecipes.collectAsStateWithLifecycle()
    val isDefaultFeedMode by viewModel.isDefaultFeedMode.collectAsStateWithLifecycle()
    val isManualRefresh by viewModel.isManualRefresh.collectAsStateWithLifecycle()
    val recipes = viewModel.pagingRecipesState.collectAsLazyPagingItems()
    val favorites = viewModel.pagingFavoritesState.collectAsLazyPagingItems()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(isManualRefresh, recipes.loadState.refresh, favorites.loadState.refresh) {
        val recipesDone = recipes.loadState.refresh !is LoadState.Loading
        val favoritesDone = favorites.loadState.refresh !is LoadState.Loading
        if (isManualRefresh && recipesDone && favoritesDone) {
            viewModel.finishManualRefresh()
        }
    }

    val recipesRefreshState = recipes.loadState.refresh
    val shouldShowFullScreenLoader =
        if (isDefaultFeedMode) {
            !hasCachedRecipes && recipesRefreshState is LoadState.Loading
        } else {
            recipesRefreshState is LoadState.Loading && recipes.itemCount == 0
        }
    val shouldShowFullScreenError =
        if (isDefaultFeedMode) {
            !hasCachedRecipes && recipesRefreshState is LoadState.Error
        } else {
            recipesRefreshState is LoadState.Error && recipes.itemCount == 0
        }
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            shouldShowFullScreenLoader -> {
                LoadingStub(Modifier.align(Alignment.Center))
            }

            shouldShowFullScreenError -> ErrorStub(
                stringResource(R.string.recipes_load_error),
                modifier = Modifier.padding(contentPadding)
            )

            else -> Content(
                searchQuery = searchQuery,
                filtersState = filtersState,
                recipes = recipes,
                favorites = favorites,
                onClick = onRecipeClick,
                onLikeClick = viewModel::likeRecipe,
                onSearch = viewModel::search,
                onRefresh = {
                    viewModel.startManualRefresh()
                    recipes.refresh()
                    favorites.refresh()
                },
                isManualRefresh = isManualRefresh,
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
    recipes: LazyPagingItems<RecipeListItemUiModel>,
    favorites: LazyPagingItems<RecipeListItemUiModel>,
    onClick: (String) -> Unit,
    onLikeClick: (RecipeListItemUiModel) -> Unit,
    onSearch: (String) -> Unit,
    onRefresh: () -> Unit,
    isManualRefresh: Boolean,
    onBrewMethodSelected: (BrewMethod) -> Unit,
    onDifficultySelected: (Difficulty?) -> Unit,
    onRoastLevelSelected: (RoastLevel?) -> Unit,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    PullToRefreshBox(isRefreshing = isManualRefresh, onRefresh = onRefresh, modifier = modifier) {
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            contentPadding = PaddingValues(
                top = Spacing.sm,
                bottom = contentPadding.calculateBottomPadding() + Spacing.xxxxl,
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

            item(FavoritesSectionKey) {
                FavoritesSection(
                    favorites = favorites,
                    onClick = onClick,
                    onLikeClick = onLikeClick,
                )
            }

            item("all_recipes_title") {
                Text(
                    stringResource(R.string.recipe_list_all_section_title),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    modifier = Modifier.padding(start = Spacing.lg)
                )
            }

            items(
                count = recipes.itemCount,
                key = { index -> recipes[index]?.id ?: "recipe_$index" },
            ) { index ->
                val recipe = recipes[index] ?: return@items
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

            if (recipes.itemCount == 0 && recipes.loadState.refresh is LoadState.NotLoading) {
                item("recipes_empty_state") {
                    RecipesEmptyPlaceholderCard(
                        text = stringResource(R.string.recipes_empty_state),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.lg)
                            .animateItem(),
                    )
                }
            }

            if (recipes.loadState.append is LoadState.Loading) {
                item("recipes_append_loading") {
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
private fun FavoritesSection(
    favorites: LazyPagingItems<RecipeListItemUiModel>,
    onClick: (String) -> Unit,
    onLikeClick: (RecipeListItemUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val favoritesListState = rememberLazyListState()

    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        modifier = modifier,
    ) {
        Text(
            stringResource(R.string.recipe_list_favorite_section_title),
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            modifier = Modifier.padding(start = Spacing.lg)
        )
        LazyRow(
            state = favoritesListState,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            contentPadding = PaddingValues(horizontal = Spacing.lg),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (favorites.itemCount == 0 && favorites.loadState.refresh is LoadState.NotLoading) {
                item(key = "favorite_empty_placeholder") {
                    FavoritesEmptyPlaceholderCard(
                        text = stringResource(R.string.recipe_list_favorite_empty_state),
                        modifier = Modifier
                            .width(FavoriteCardWidth)
                            .animateItem(),
                    )
                }
            }

            items(
                count = favorites.itemCount,
                key = { index -> favorites[index]?.id ?: "favorite_$index" },
            ) { index ->
                val item = favorites[index] ?: return@items
                RecipeCompactCard(
                    item = item,
                    modifier = Modifier.width(FavoriteCardWidth).animateItem(),
                    onClick = { onClick(item.id) },
                    onLikeClick = { onLikeClick(item) },
                )
            }

            if (favorites.loadState.append is LoadState.Loading) {
                item(key = "favorite_append_loading") {
                    Box(
                        modifier = Modifier
                            .width(FavoriteCardWidth)
                            .padding(vertical = Spacing.lg),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
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
private fun FavoritesEmptyPlaceholderCard(
    text: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.height(FavoriteCardHeight),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(Spacing.md),
            )
        }
    }
}

@Composable
private fun RecipesEmptyPlaceholderCard(
    text: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(RecipeCardHeight),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.lg),
            )
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

@Preview
@Composable
private fun FavoritesEmptyPlaceholderCardPreview() {
    RoastiTheme {
        FavoritesEmptyPlaceholderCard(
            text = "You don't have favorites yet",
            modifier = Modifier.width(FavoriteCardWidth),
        )
    }
}

@Preview
@Composable
private fun RecipesEmptyPlaceholderCardPreview() {
    RoastiTheme {
        RecipesEmptyPlaceholderCard(
            text = "No recipes found",
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
