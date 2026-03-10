package org.nikol.roasti.ui.features.recipepage

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.nikol.roasti.R
import org.nikol.roasti.recipe.model.BrewMethod
import org.nikol.roasti.recipe.model.BrewStep
import org.nikol.roasti.recipe.model.Difficulty
import org.nikol.roasti.recipe.model.Recipe
import org.nikol.roasti.recipe.model.RoastLevel
import org.nikol.roasti.ui.theme.Orange600
import org.nikol.roasti.ui.theme.RoastiTheme
import org.nikol.roasti.ui.theme.ShapeXxl
import org.nikol.roasti.ui.theme.Spacing
import org.nikol.roasti.ui.theme.Stone100
import org.nikol.roasti.ui.uikit.AsyncImagePreviewProvider
import org.nikol.roasti.ui.uikit.ErrorStub
import org.nikol.roasti.ui.uikit.LoadingStub
import kotlin.time.Duration.Companion.seconds

private const val RecipeScreenKeyPrefix = "recipe_screen_"
private val HeaderHeight = 300.dp
private val HeaderOverlap = 56.dp
private val MetaCardHeight = 88.dp
private val StepNumberSize = 32.dp
private val BackButtonSize = 40.dp
private val PrimaryButtonHeight = 56.dp
private val ContentShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
private val StepShape = RoundedCornerShape(16.dp)
private val MetaCardShape = RoundedCornerShape(14.dp)
private const val BackLabel = "<"
private const val StartArrow = ">"

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun RecipeContentRoute(
    id: String,
    onBackClick: () -> Unit = {},
    onStartBrewing: (startStep: Int) -> Unit = {},
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {

    val viewModel: RecipeContentViewModel = koinViewModel(parameters = { parametersOf(id) })
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (state) {
        RecipeContentState.Loading -> LoadingStub()
        RecipeContentState.Error -> ErrorStub(stringResource(R.string.error_generic))
        RecipeContentState.NotFound -> ErrorStub(stringResource(R.string.recipe_not_found))
        is RecipeContentState.Content -> RecipeContentScreen(
            state = state as RecipeContentState.Content,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
            onBackClick = onBackClick,
            onStepClick = onStartBrewing,
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun RecipeContentScreen(
    state: RecipeContentState.Content,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    onBackClick: () -> Unit = {},
    onStepClick: (stepIndex: Int) -> Unit = {},
) {
    val recipe = state.recipe
    val recipeScreenModifier = recipeScreenSharedBoundsModifier(
        recipeId = recipe.id,
        sharedTransitionScope = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope,
    )

    val stepModifiers = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        recipe.steps.indices.map { index ->
            with(sharedTransitionScope) {
                Modifier.sharedBounds(
                    sharedContentState = rememberSharedContentState(key = "brew_step_$index"),
                    animatedVisibilityScope = animatedVisibilityScope,
                )
            }
        }
    } else emptyList()

    Box(
        modifier = recipeScreenModifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        RecipeHeaderImage(imageUrl = recipe.imageUrl)
        RecipeContentList(
            recipe = recipe,
            stepModifiers = stepModifiers,
            onStepClick = onStepClick,
            modifier = Modifier.fillMaxSize(),
        )
        BackButton(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = Spacing.lg, top = Spacing.lg)
                .clickable { onBackClick() }
        )
    }
}

@Composable
private fun RecipeHeaderImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(HeaderHeight)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background.copy(alpha = 0.04f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.16f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.4f),
                        )
                    )
                )
        )
    }
}

@Composable
private fun RecipeContentList(
    recipe: Recipe,
    stepModifiers: List<Modifier> = emptyList(),
    onStepClick: (stepIndex: Int) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = Spacing.xxxl),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        item {
            Spacer(modifier = Modifier.height(HeaderHeight - HeaderOverlap))
        }
        item {
            RecipeMainContent(
                recipe = recipe,
                stepModifiers = stepModifiers,
                onStepClick = onStepClick,
            )
        }
    }
}

@Composable
private fun RecipeMainContent(
    recipe: Recipe,
    stepModifiers: List<Modifier> = emptyList(),
    onStepClick: (stepIndex: Int) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ContentShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = Spacing.xxl, vertical = Spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(Spacing.xl),
    ) {
        Text(
            text = recipe.title,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = recipe.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        RecipeMetaGrid(recipe = recipe)
        recipe.beans?.let { beans ->
            RecipeTextSection(
                title = stringResource(R.string.recipe_beans),
                value = beans,
            )
        }
        recipe.roastLevel?.let { roastLevel ->
            RecipeTextSection(
                title = stringResource(R.string.recipe_roast_level),
                value = roastLevel.displayName,
            )
        }
        RecipeStepsSection(
            steps = recipe.steps,
            stepModifiers = stepModifiers,
            onStepClick = onStepClick,
        )
        StartBrewingButton(
            onClick = { onStepClick(0) },
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        )
    }
}

@Composable
private fun RecipeMetaGrid(
    recipe: Recipe,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            modifier = Modifier.fillMaxWidth(),
        ) {
            MetaCard(
                title = stringResource(R.string.recipe_brew_method),
                value = recipe.brewMethod.displayName,
                highlight = true,
                modifier = Modifier.weight(1f),
            )
            MetaCard(
                title = stringResource(R.string.recipe_time),
                value = formatBrewTime(recipe.totalBrewTimeSeconds),
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            modifier = Modifier.fillMaxWidth(),
        ) {
            MetaCard(
                title = stringResource(R.string.recipe_roaster),
                value = recipe.beans ?: stringResource(R.string.recipe_missing_value),
                modifier = Modifier.weight(1f),
            )
            MetaCard(
                title = stringResource(R.string.recipe_difficulty),
                value = recipe.difficulty.displayName,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MetaCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
) {
    val backgroundColor = if (highlight) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val borderColor = if (highlight) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.outline
    }
    val titleColor = if (highlight) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = modifier
            .height(MetaCardHeight)
            .clip(MetaCardShape)
            .background(backgroundColor)
            .border(width = 1.dp, color = borderColor, shape = MetaCardShape)
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = titleColor,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
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
private fun RecipeTextSection(
    title: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun RecipeStepsSection(
    steps: List<BrewStep>,
    stepModifiers: List<Modifier> = emptyList(),
    onStepClick: (stepIndex: Int) -> Unit = {},
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        Text(
            text = stringResource(R.string.recipe_brewing_steps),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        steps.forEachIndexed { index, step ->
            BrewStepCard(
                step = step,
                modifier = stepModifiers.getOrElse(index) { Modifier },
                onClick = { onStepClick(index) },
            )
        }
    }
}

@Composable
private fun BrewStepCard(
    step: BrewStep,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(StepShape)
            .background(MaterialTheme.colorScheme.background)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = StepShape)
            .clickable(onClick = onClick)
            .padding(Spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(StepNumberSize)
                .clip(CircleShape)
                .background(Stone100),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = step.order.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Text(
                text = step.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = step.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            step.durationSeconds?.let { duration ->
                Text(
                    text = formatStepDuration(duration),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun StartBrewingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(PrimaryButtonHeight),
        colors = ButtonDefaults.buttonColors(
            containerColor = Orange600,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        shape = ShapeXxl,
    ) {
        Text(
            text = "$StartArrow  ${stringResource(R.string.recipe_start_brewing)}",
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun BackButton(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.size(BackButtonSize),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        tonalElevation = 2.dp,
        shadowElevation = 6.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = BackLabel,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun formatBrewTime(totalSeconds: Int): String {
    val minutes = totalSeconds.seconds.inWholeMinutes
    return if (minutes > 0) {
        minutes.toInt().let { stringResource(R.string.recipe_time_minutes, it) }
    } else {
        stringResource(R.string.recipe_missing_value)
    }
}

private fun formatStepDuration(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun RecipeContentScreenPreview() {
    RoastiTheme {
        AsyncImagePreviewProvider {
            RecipeContentScreen(
                RecipeContentState.Content(
                    recipe = Recipe(
                        id = "classic-pour-over",
                        title = "Classic Pour Over",
                        description = "A bright and floral pour over with notes of blueberry and citrus.",
                        imageUrl = "https://example.com/coffee.jpg",
                        brewMethod = BrewMethod.V60,
                        difficulty = Difficulty.Medium,
                        totalBrewTimeSeconds = 240,
                        roastLevel = RoastLevel.Light,
                        beans = "Ethiopian Yirgacheffe",
                        steps = listOf(
                            BrewStep(
                                order = 1,
                                title = "Prepare Equipment",
                                description = "Place filter in V60 and rinse with hot water. Discard rinse water.",
                                durationSeconds = 30,
                            ),
                            BrewStep(
                                order = 2,
                                title = "Add Coffee",
                                description = "Add 20g of medium-fine ground coffee to the filter.",
                                durationSeconds = 15,
                            ),
                            BrewStep(
                                order = 3,
                                title = "Bloom",
                                description = "Pour 40g of water in a circular motion. Let bloom for 30 seconds.",
                                durationSeconds = 30,
                            ),
                        ),
                    )
                )
            )
        }
    }
}
