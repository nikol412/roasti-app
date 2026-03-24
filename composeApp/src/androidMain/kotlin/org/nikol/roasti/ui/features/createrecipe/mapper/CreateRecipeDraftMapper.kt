package org.nikol.roasti.ui.features.createrecipe.mapper

import org.nikol.roasti.feature.recipe.domain.model.BrewMethod
import org.nikol.roasti.feature.recipe.domain.model.RecipeDraft
import org.nikol.roasti.feature.recipe.domain.model.RecipeDraftStep
import org.nikol.roasti.feature.recipe.domain.model.RoastLevel
import org.nikol.roasti.ui.features.createrecipe.model.CreateRecipeStepUiModel
import org.nikol.roasti.ui.features.createrecipe.model.CreateRecipeUiState

internal fun CreateRecipeUiState.toRecipeDraft() = RecipeDraft(
    title = name,
    description = description,
    imageId = imageId,
    brewMethod = brewMethod ?: BrewMethod.NONE,
    difficulty = difficulty,
    roastLevel = roastLevel ?: RoastLevel.NONE,
    beans = beans,
    steps = brewSteps.mapIndexed { index, item -> item.toRecipeDraftStep(index) },
)

private fun CreateRecipeStepUiModel.toRecipeDraftStep(index: Int) = RecipeDraftStep(
    order = index,
    title = title,
    description = description.takeIf { it.isNotBlank() },
    durationSeconds = durationInSeconds,
    imageId = imageId,
)
