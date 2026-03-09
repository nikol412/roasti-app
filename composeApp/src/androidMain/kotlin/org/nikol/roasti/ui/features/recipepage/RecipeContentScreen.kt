package org.nikol.roasti.ui.features.recipepage

import androidx.activity.compose.BackHandler
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

private val HeaderHeight = 300.dp
private val HeaderOverlap = 36.dp
private val MetaCardHeight = 88.dp
private val StepNumberSize = 32.dp
private val BackButtonSize = 40.dp
private val PrimaryButtonHeight = 56.dp
private val ContentShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
private val StepShape = RoundedCornerShape(16.dp)
private val MetaCardShape = RoundedCornerShape(14.dp)
private const val BackLabel = "<"
private const val StartArrow = ">"

@Composable
fun RecipeContentScreen(id: String, onBackClick: () -> Unit = {}) {

    BackHandler { onBackClick() }

    val viewModel: RecipeContentViewModel = koinViewModel(parameters = { parametersOf(id) })
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (state) {
        RecipeContentState.Loading -> LoadingStub()
        RecipeContentState.NotFound -> ErrorStub(stringResource(R.string.recipe_not_found))
        is RecipeContentState.Content -> Content(state as RecipeContentState.Content, onBackClick)
    }
}

@Composable
private fun Content(state: RecipeContentState.Content, onBackClick: () -> Unit = {}) {
    val recipe = state.recipe

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        RecipeHeaderImage(recipe.imageUrl)
        RecipeContentList(
            recipe = recipe,
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
private fun RecipeHeaderImage(imageUrl: String?) {
    Box(
        modifier = Modifier
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
            RecipeMainContent(recipe = recipe)
        }
    }
}

@Composable
private fun RecipeMainContent(recipe: Recipe) {
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
        RecipeStepsSection(steps = recipe.steps)
        StartBrewingButton(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        )
    }
}

@Composable
private fun RecipeMetaGrid(recipe: Recipe) {
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
private fun RecipeStepsSection(steps: List<BrewStep>) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        Text(
            text = stringResource(R.string.recipe_brewing_steps),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        steps.forEach { step ->
            BrewStepCard(step = step)
        }
    }
}

@Composable
private fun BrewStepCard(step: BrewStep) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(StepShape)
            .background(MaterialTheme.colorScheme.background)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = StepShape)
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
private fun StartBrewingButton(modifier: Modifier = Modifier) {
    Button(
        onClick = {},
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
            Content(
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
                            BrewStep(
                                order = 4,
                                title = "First Pour",
                                description = "Pour water up to 120g in a slow circular motion.",
                                durationSeconds = 45,
                            ),
                            BrewStep(
                                order = 5,
                                title = "Second Pour",
                                description = "Pour water up to 220g. Maintain steady pace.",
                                durationSeconds = 45,
                            ),
                            BrewStep(
                                order = 6,
                                title = "Final Pour",
                                description = "Pour remaining water to reach 320g total. Let drain completely.",
                                durationSeconds = 90,
                            ),
                            BrewStep(
                                order = 7,
                                title = "Enjoy",
                                description = "Remove V60, swirl carafe gently, and pour into your favorite cup.",
                                durationSeconds = null,
                            ),
                        ),
                    )
                )
            )
        }
    }
}
