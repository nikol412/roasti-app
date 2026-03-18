package org.nikol.roasti.ui.features.editrecipe.mapper

import org.nikol.roasti.domain.recipe.model.RecipeDraft
import org.nikol.roasti.domain.recipe.model.RecipeDraftStep
import org.nikol.roasti.ui.features.editrecipe.model.EditRecipeStepUiModel
import org.nikol.roasti.ui.features.editrecipe.model.EditRecipeUiState

internal fun EditRecipeUiState.toRecipeDraft() = RecipeDraft(
    title = title,
    description = description,
    imageId = imageId,
    brewMethod = brewMethod,
    difficulty = difficulty,
    roastLevel = roastLevel,
    beans = beans.takeIf { it.isNotBlank() },
    steps = steps.mapIndexed { index, step -> step.toRecipeDraftStep(index) },
)

private fun EditRecipeStepUiModel.toRecipeDraftStep(index: Int) = RecipeDraftStep(
    order = index,
    title = title,
    description = description.takeIf { it.isNotBlank() },
    durationSeconds = durationSeconds,
    imageId = imageId,
)
