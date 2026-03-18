package org.nikol.roasti.ui.features.editrecipe.mapper

import org.nikol.roasti.domain.recipe.model.BrewStep
import org.nikol.roasti.domain.recipe.model.Recipe
import org.nikol.roasti.ui.features.editrecipe.model.EditRecipeStepUiModel
import org.nikol.roasti.ui.features.editrecipe.model.EditRecipeUiState
import org.nikol.roasti.utils.imageUrl

internal fun Recipe.toEditState() = EditRecipeUiState(
    isLoading = false,
    title = title,
    description = description,
    imageId = imageId,
    imageUrl = imageId?.let(::imageUrl),
    brewMethod = brewMethod,
    difficulty = difficulty,
    roastLevel = roastLevel,
    beans = beans ?: "",
    steps = steps.map(BrewStep::toEditStepModel),
)

private fun BrewStep.toEditStepModel() = EditRecipeStepUiModel(
    order = order,
    title = title,
    description = description,
    durationSeconds = durationSeconds,
    imageId = imageId,
)
