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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import org.koin.compose.viewmodel.koinViewModel
import org.nikol.roasti.R
import org.nikol.roasti.domain.recipe.model.BrewMethod
import org.nikol.roasti.domain.recipe.model.Difficulty
import org.nikol.roasti.ui.features.createrecipe.CreateRecipeSheet
import org.nikol.roasti.ui.features.recipe.mapper.labelRes
import org.nikol.roasti.ui.features.recipelist.model.RecipeListItemUiModel
import org.nikol.roasti.ui.theme.RoastiTheme
import org.nikol.roasti.ui.theme.RoastiTypography
import org.nikol.roasti.ui.theme.Spacing
import org.nikol.roasti.ui.uikit.AsyncImagePreviewProvider
import org.nikol.roasti.ui.uikit.ErrorStub
import org.nikol.roasti.presentation.recipe.filter.RecipeFilterState

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

    val snackbarHostState = remember { SnackbarHostState() }

    Box(modifier = Modifier.fillMaxSize()) {
        when (state) {
            RecipesListState.Loading -> Loading(
                Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            )

            RecipesListState.Error -> ErrorStub(
                stringResource(R.string.recipes_load_error),
                modifier = Modifier.padding(contentPadding)
            )

            is RecipesListState.Content -> Content(
                filtersState = filtersState,
                state = state as RecipesListState.Content,
                onClick = onRecipeClick,
                onLoadMore = viewModel::loadNextPage,
                onRefresh = { viewModel.reload() },
                onBrewMethodSelected = { viewModel.filterByBrewMethod(it) },
                onDifficultySelected = { viewModel.filterByDifficulty(it) },
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

    // Full-screen create is now handled via navigation (CreateRecipeRoute).
    // Modal sheet kept below for reference — uncomment to compare both approaches.
    //
    // var showCreateSheet by remember { mutableStateOf(false) }
    // if (showCreateSheet) {
    //     CreateRecipeSheet(
    //         onDismiss = { showCreateSheet = false },
    //         onPublished = {
    //             showCreateSheet = false
    //             coroutineScope.launch {
    //                 snackbarHostState.showSnackbar(if (it) "Recipe created" else "something went wrong, try again")
    //             }
    //             viewModel.reload()
    //         },
    //     )
    // }
}

@Composable
private fun Loading(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun Content(
    filtersState: RecipeFilterState,
    state: RecipesListState.Content,
    onClick: (String) -> Unit,
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit,
    onBrewMethodSelected: (BrewMethod?) -> Unit,
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
            contentPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding() + 16.dp)
        ) {

            stickyHeader {
                Surface(Modifier.fillMaxWidth()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        BrewMethodSelector(filtersState.brewMethod, onBrewMethodSelected)
                        DifficultySelector(filtersState.difficulty, onDifficultySelected)
                    }
                }
            }
            items(state.recipes, key = { it.id }) { recipe ->
                RecipeItem(
                    item = recipe,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClick(recipe.id) },
                )
            }
            if (state.isLoadingMore) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .padding(16.dp),
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun RecipeItem(
    item: RecipeListItemUiModel,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    modifier: Modifier = Modifier,
) {
    val sharedBoundsModifier = recipeScreenSharedBoundsModifier(
        recipeId = item.id,
        sharedTransitionScope = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope,
    )

    Row(
        modifier = modifier
            .then(sharedBoundsModifier)
            .clip(RoundedCornerShape(8.dp)),
    ) {
        RecipeImage(url = item.imageUrl)
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                item.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = RoastiTypography.titleMedium
            )
            Text(
                item.description,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
                style = RoastiTypography.bodyMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    stringResource(item.brewMethodLabelRes),
                    style = RoastiTypography.bodyMedium,
                )
                Text(
                    stringResource(item.difficultyLabelRes),
                    style = RoastiTypography.bodyMedium,
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun recipeScreenSharedBoundsModifier(
    recipeId: String,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
): Modifier {
    if (sharedTransitionScope == null || animatedVisibilityScope == null) {
        return Modifier
    }

    return with(sharedTransitionScope) {
        Modifier.sharedBounds(
            sharedContentState = rememberSharedContentState(key = "$RecipeScreenKeyPrefix$recipeId"),
            animatedVisibilityScope = animatedVisibilityScope,
        )
    }
}

@Composable
private fun RecipeImage(url: String?, modifier: Modifier = Modifier) {
    AsyncImage(
        model = url,
        contentDescription = null,
        placeholder = null,
        modifier = modifier.size(120.dp),
    )
}

@Composable
private fun BrewMethodSelector(selectedValue: BrewMethod?, onSelected: (BrewMethod?) -> Unit) {
    val methods = BrewMethod.entries.filterNot { it == BrewMethod.NONE }.toTypedArray()
    var expanded by remember { mutableStateOf(false) }

    Box {
        AssistChip(
            onClick = { expanded = true },
            label = {
                if (selectedValue != null) Text(stringResource(selectedValue.labelRes())) else Text(stringResource(R.string.recipe_brew_method))
            },
            trailingIcon = {
                Text("☕️")
            },
            colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            methods.forEach { method ->
                DropdownMenuItem(
                    text = { Text(stringResource(method.labelRes())) },
                    onClick = {
                        onSelected(method)
                        expanded = false
                    }
                )
            }
            Box {
                HorizontalDivider(Modifier.matchParentSize())
                DropdownMenuItem(
                    text = { Text("clear") },
                    onClick = {
                        onSelected(null)
                        expanded = false
                    },
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun DifficultySelector(selectedValue: Difficulty?, onSelected: (Difficulty?) -> Unit) {
    val methods = Difficulty.entries.toTypedArray()
    var expanded by remember { mutableStateOf(false) }

    Box {
        AssistChip(
            onClick = { expanded = true },
            label = {
                if (selectedValue != null) Text(stringResource(selectedValue.labelRes())) else Text(stringResource(R.string.recipe_difficulty))
            },
            trailingIcon = {
                Text("📈️")
            },
            colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            methods.forEach { method ->
                DropdownMenuItem(
                    text = { Text(stringResource(method.labelRes())) },
                    onClick = {
                        onSelected(method)
                        expanded = false
                    }
                )
            }
            Box {
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("clear") },
                    onClick = {
                        onSelected(null)
                        expanded = false
                    },
                    modifier = Modifier
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RecipeItemPreview() {
    RoastiTheme {
        AsyncImagePreviewProvider {
            RecipeItem(
                RecipeListItemUiModel(
                    id = "erat",
                    title = "reformidans reformidans reformidans",
                    description = "eos",
                    imageUrl = null,
                    brewMethodLabelRes = R.string.recipe_brew_method_v60,
                    difficultyLabelRes = R.string.recipe_difficulty_easy,
                )
            )
        }
    }
}

@Preview
@Composable
private fun ImagePreview() {
    RoastiTheme {
        AsyncImagePreviewProvider {
            RecipeImage("", Modifier.size(20.dp))
        }
    }
}
