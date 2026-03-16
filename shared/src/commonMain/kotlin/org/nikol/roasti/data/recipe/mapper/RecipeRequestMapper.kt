package org.nikol.roasti.data.recipe.mapper

import org.nikol.roasti.data.recipe.dto.BrewStepDto
import org.nikol.roasti.data.recipe.dto.RecipeDto
import org.nikol.roasti.data.recipe.request.UploadRecipeRequestBody
import org.nikol.roasti.data.recipe.request.UploadRecipeStepRequestBody

fun RecipeDto.toRequest() = UploadRecipeRequestBody(
    title, beans, brewMethod, description, difficulty, imageId, roastLevel,
    steps.orEmpty().map { it.toRequest() },
)

fun BrewStepDto.toRequest() = UploadRecipeStepRequestBody(
    description, durationSeconds, imageId, order, title,
)
