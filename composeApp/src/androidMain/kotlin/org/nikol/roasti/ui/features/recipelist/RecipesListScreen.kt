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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import org.nikol.roasti.recipe.model.BrewMethod
import org.nikol.roasti.recipe.model.Difficulty
import org.nikol.roasti.recipe.model.Recipe
import org.nikol.roasti.recipe.model.RoastLevel
import org.nikol.roasti.ui.theme.RoastiTheme
import org.nikol.roasti.ui.theme.RoastiTypography
import org.nikol.roasti.ui.uikit.AsyncImagePreviewProvider
import org.nikol.roasti.ui.uikit.ErrorStub
import kotlin.time.Duration.Companion.seconds

private const val RecipeScreenKeyPrefix = "recipe_screen_"

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun RecipesListScreen(
    onRecipeClick: (Recipe) -> Unit = {},
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    contentPadding: PaddingValues,
) {
    val viewModel: RecipesListViewModel = koinViewModel()

    val state by viewModel.recipes.collectAsStateWithLifecycle()

    when (state) {
        RecipesListState.Loading -> Loading(Modifier
            .fillMaxSize()
            .padding(contentPadding))
        RecipesListState.Error -> ErrorStub(
            stringResource(R.string.recipes_load_error),
            modifier = Modifier.padding(contentPadding)
        )

        is RecipesListState.Content -> Content(
            state = state as RecipesListState.Content,
            onClick = onRecipeClick,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
            contentPadding = contentPadding,
        )
    }
}

@Composable
private fun Loading(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.width(64.dp),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun Content(
    state: RecipesListState.Content,
    onClick: (Recipe) -> Unit,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier,
        contentPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding() + 16.dp)
    ) {
        items(state.recipes, key = { it.id }) { recipe ->
            RecipeItem(
                item = recipe,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                modifier = Modifier.clickable { onClick(recipe) },
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun RecipeItem(
    item: Recipe,
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
                    item.brewMethod.displayName,
                    style = RoastiTypography.bodyMedium,
                )
                Text(
                    item.difficulty.displayName,
                    style = RoastiTypography.bodyMedium,
                )
                Text(
                    "${item.totalBrewTimeSeconds.seconds.inWholeMinutes} min",
                    style = RoastiTypography.bodyMedium
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

@Preview(showBackground = true)
@Composable
private fun RecipeItemPreview() {
    RoastiTheme {
        AsyncImagePreviewProvider {
            RecipeItem(
                Recipe(
                    id = "erat",
                    title = "reformidans reformidans reformidans",
                    description = "eos",
                    imageUrl = "https://search.yahoo.com/search?p=singulis",
                    brewMethod = BrewMethod.V60,
                    difficulty = Difficulty.Easy,
                    totalBrewTimeSeconds = 2895,
                    roastLevel = RoastLevel.Light,
                    beans = "vocibus",
                    steps = listOf()
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
