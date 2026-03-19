package org.nikol.roasti.ui.features.recipepage

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.nikol.roasti.R
import org.nikol.roasti.ui.features.recipepage.model.RecipeDetailsUiModel
import org.nikol.roasti.ui.features.recipepage.model.RecipeStepUiModel
import org.nikol.roasti.ui.theme.RoastiTheme
import org.nikol.roasti.ui.theme.ShapeXxl
import org.nikol.roasti.ui.theme.Spacing
import org.nikol.roasti.ui.uikit.AsyncImagePreviewProvider
import org.nikol.roasti.ui.uikit.ErrorStub
import org.nikol.roasti.ui.uikit.LoadingStub

private const val RecipeScreenKeyPrefix = "recipe_screen_"
private val HeaderHeight = 220.dp
private val HeaderOverlap = 40.dp
private val MetaChipShape = RoundedCornerShape(18.dp)
private val StepNumberSize = 28.dp
private val StepDurationShape = RoundedCornerShape(10.dp)
private val HeaderActionButtonHeight = 40.dp
private val PrimaryButtonHeight = 56.dp
private val ContentShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
private val HeaderActionShape = RoundedCornerShape(20.dp)
private const val BackLabel = "<"
private const val StartArrow = ">"

private data class RecipeMetaItem(
    val title: String,
    val value: String,
    val isHighlighted: Boolean = false,
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun RecipeContentRoute(
    id: String,
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onStartBrewing: (startStep: Int) -> Unit = {},
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {

    val viewModel: RecipeContentViewModel = koinViewModel(parameters = { parametersOf(id) })
    val state by viewModel.state.collectAsStateWithLifecycle()


    var showRemoveDialogConfirmation: Boolean by remember { mutableStateOf(false) }

    if (showRemoveDialogConfirmation) {
        RemoveConfirmationDialog(
            onConfirmRemove = {
                viewModel.onRemoveRecipe()
                showRemoveDialogConfirmation = false
            },
            onDismiss = {
                showRemoveDialogConfirmation = false
            }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                RecipeContentEvent.NavigateBack -> onBackClick()
            }
        }
    }

    when (state) {
        RecipeContentState.Loading -> LoadingStub()
        RecipeContentState.Error -> ErrorStub(stringResource(R.string.error_generic))
        RecipeContentState.NotFound -> ErrorStub(stringResource(R.string.recipe_not_found))
        is RecipeContentState.Content -> RecipeContentScreen(
            state = state as RecipeContentState.Content,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
            onBackClick = onBackClick,
            onEditClick = onEditClick,
            onRemoveClick = { showRemoveDialogConfirmation = true },
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
    onEditClick: () -> Unit = {},
    onRemoveClick: () -> Unit = {},
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            RecipeBottomBar(
                enabled = recipe.steps.isNotEmpty(),
                onClick = {
                    if (recipe.steps.isNotEmpty()) {
                        onStepClick(0)
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = recipeScreenModifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            RecipeHeaderImage(imageUrl = recipe.imageUrl)
            RecipeContentList(
                recipe = recipe,
                stepModifiers = stepModifiers,
                bottomContentPadding = innerPadding.calculateBottomPadding(),
                modifier = Modifier.fillMaxSize(),
            )
            RecipeTopBar(
                onBackClick = onBackClick,
                onEditClick = onEditClick,
                onRemoveClick = onRemoveClick,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
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
    recipe: RecipeDetailsUiModel,
    stepModifiers: List<Modifier> = emptyList(),
    bottomContentPadding: Dp = 0.dp,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = Spacing.xxxl + bottomContentPadding),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        item {
            Spacer(modifier = Modifier.height(HeaderHeight - HeaderOverlap))
        }
        item {
            RecipeMainContent(
                recipe = recipe,
                stepModifiers = stepModifiers,
            )
        }
    }
}

@Composable
private fun RecipeMainContent(
    recipe: RecipeDetailsUiModel,
    stepModifiers: List<Modifier> = emptyList(),
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
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        RecipeMetaSection(recipe = recipe)
        RecipeStepsSection(
            steps = recipe.steps,
            stepModifiers = stepModifiers,
        )
    }
}

@Composable
private fun RecipeTopBar(
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = Spacing.lg, vertical = Spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeaderActionButton(
            text = BackLabel,
            onClick = onBackClick,
            modifier = Modifier.size(HeaderActionButtonHeight),
        )

        Spacer(Modifier.weight(1f))
        HeaderActionButton(
            text = stringResource(R.string.recipe_edit),
            onClick = onEditClick,
            modifier = Modifier
                .height(HeaderActionButtonHeight)
                .padding(end = 16.dp),
        )

        HeaderActionButton(
            text = stringResource(R.string.recipe_remove),
            onClick = onRemoveClick,
            modifier = Modifier.height(HeaderActionButtonHeight),
            highlightDangerous = true,
        )
    }
}

@Composable
private fun HeaderActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    highlightDangerous: Boolean = false,
) {
    Surface(
        modifier = modifier
            .clip(HeaderActionShape)
            .clickable(onClick = onClick),
        shape = HeaderActionShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        tonalElevation = 2.dp,
        shadowElevation = 6.dp,
    ) {
        Box(
            modifier = Modifier
                .defaultMinSize(minWidth = HeaderActionButtonHeight)
                .padding(horizontal = Spacing.lg),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = if (highlightDangerous) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun RecipeMetaSection(
    recipe: RecipeDetailsUiModel,
) {
    val items = buildList {
        add(
            RecipeMetaItem(
                title = stringResource(R.string.recipe_brew_method),
                value = stringResource(recipe.brewMethodLabelRes),
                isHighlighted = true,
            )
        )
        add(
            RecipeMetaItem(
                title = stringResource(R.string.recipe_difficulty),
                value = stringResource(recipe.difficultyLabelRes),
            )
        )
        recipe.beans?.takeIf { it.isNotBlank() }?.let { beans ->
            add(
                RecipeMetaItem(
                    title = stringResource(R.string.recipe_beans),
                    value = beans,
                )
            )
        }
        recipe.roastLevelLabelRes?.let { roastLevelLabelRes ->
            add(
                RecipeMetaItem(
                    title = stringResource(R.string.recipe_roast_level),
                    value = stringResource(roastLevelLabelRes),
                )
            )
        }
    }

    RecipeMetaChips(items = items)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecipeMetaChips(
    items: List<RecipeMetaItem>,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        modifier = Modifier.fillMaxWidth(),
    ) {
        items.forEach { item ->
            RecipeMetaChip(item = item)
        }
    }
}

@Composable
private fun RecipeMetaChip(
    item: RecipeMetaItem,
) {
    val backgroundColor = if (item.isHighlighted) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }
    val titleColor = if (item.isHighlighted) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = Modifier
            .clip(MetaChipShape)
            .background(backgroundColor)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Text(
            text = item.title,
            style = MaterialTheme.typography.labelSmall,
            color = titleColor,
        )
        Text(
            text = item.value,
            style = MaterialTheme.typography.titleSmall,
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
private fun RecipeStepsSection(
    steps: List<RecipeStepUiModel>,
    stepModifiers: List<Modifier> = emptyList(),
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Text(
            text = stringResource(R.string.recipe_brewing_steps),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Column {
            steps.forEachIndexed { index, step ->
                BrewStepItem(
                    step = step,
                    stepNumber = index + 1,
                    modifier = stepModifiers.getOrElse(index) { Modifier },
                )
                if (index != steps.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = StepNumberSize + Spacing.md),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun BrewStepItem(
    step: RecipeStepUiModel,
    stepNumber: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(StepNumberSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stepNumber.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = step.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            step.durationSeconds?.let { duration ->
                StepDurationChip(duration = formatStepDuration(duration))
            }
        }
    }
}

@Composable
private fun StepDurationChip(
    duration: String,
) {
    Box(
        modifier = Modifier
            .clip(StepDurationShape)
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
    ) {
        Text(
            text = duration,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.tertiary,
        )
    }
}

@Composable
private fun StartBrewingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(PrimaryButtonHeight),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        shape = ShapeXxl,
        enabled = enabled,
    ) {
        Text(
            text = "$StartArrow  ${stringResource(R.string.recipe_start_brewing)}",
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun RecipeBottomBar(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                        0.32f to MaterialTheme.colorScheme.surface.copy(alpha = 0.48f),
                        0.62f to MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
                        1.0f to MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                    )
                )
            )
    ) {
        StartBrewingButton(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    start = Spacing.xxl,
                    top = 0.dp,
                    end = Spacing.xxl,
                    bottom = Spacing.lg,
                ),
            enabled = enabled,
        )
    }
}

@Composable
private fun RemoveConfirmationDialog(onConfirmRemove: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_confirm_title)) },
        confirmButton = {
            TextButton(onClick = onConfirmRemove) {
                Text(
                    text = stringResource(R.string.dialog_confirm_title),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_dismiss_label))
            }
        },
    )
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
                    recipe = RecipeDetailsUiModel(
                        id = "aeropress-inverted",
                        title = "Aeropress Inverted",
                        description = "Clean and sweet cup with balanced acidity and a compact recipe flow that stays readable even with multiple brewing steps.",
                        imageUrl = null,
                        brewMethodLabelRes = R.string.recipe_brew_method_aeropress,
                        difficultyLabelRes = R.string.recipe_difficulty_medium,
                        roastLevelLabelRes = R.string.recipe_roast_level_medium,
                        beans = "Colombian Supremo",
                        steps = listOf(
                            RecipeStepUiModel(
                                order = 1,
                                title = "Setup Inverted",
                                description = "Assemble the Aeropress in inverted position with the plunger seated securely.",
                                durationSeconds = 30,
                            ),
                            RecipeStepUiModel(
                                order = 2,
                                title = "Add Coffee",
                                description = "Add 17 g of medium-fine ground coffee and level the bed.",
                                durationSeconds = 15,
                            ),
                            RecipeStepUiModel(
                                order = 3,
                                title = "First Pour",
                                description = "Pour in hot water to half volume and make sure all grounds are saturated.",
                                durationSeconds = 30,
                            ),
                            RecipeStepUiModel(
                                order = 4,
                                title = "Stir",
                                description = "Stir gently for a few seconds to break crust and improve extraction.",
                                durationSeconds = 10,
                            ),
                            RecipeStepUiModel(
                                order = 5,
                                title = "Top Up",
                                description = "Add the remaining water, attach the filter cap, and wait briefly.",
                                durationSeconds = 35,
                            ),
                            RecipeStepUiModel(
                                order = 6,
                                title = "Flip And Press",
                                description = "Carefully flip onto the cup and press slowly until you hear a hiss.",
                                durationSeconds = 60,
                            ),
                        ),
                    )
                )
            )
        }
    }
}
