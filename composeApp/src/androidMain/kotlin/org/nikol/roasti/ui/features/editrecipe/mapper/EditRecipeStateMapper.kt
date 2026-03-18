package org.nikol.roasti.ui.features.editrecipe.mapper

import org.nikol.roasti.domain.recipe.model.BrewStep
import org.nikol.roasti.domain.recipe.model.Recipe
import org.nikol.roasti.ui.features.editrecipe.model.EditRecipeUiState
import org.nikol.roasti.ui.features.recipeform.model.RecipeFormFields
import org.nikol.roasti.ui.features.recipeform.model.RecipeFormStepUiModel
import org.nikol.roasti.utils.imageUrl

internal fun Recipe.toEditState() = EditRecipeUiState(
    isLoading = false,
    form = RecipeFormFields(
        title = title,
        description = description,
        imageId = imageId,
        imageUrl = imageId?.let(::imageUrl),
        brewMethod = brewMethod,
        difficulty = difficulty,
        roastLevel = roastLevel,
        beans = beans ?: "",
        steps = steps.map(BrewStep::toFormStep),
    ),
)

private fun BrewStep.toFormStep() = RecipeFormStepUiModel(
    order = order,
    title = title,
    description = description ?: "",
    durationSeconds = durationSeconds,
    imageId = imageId,
)
