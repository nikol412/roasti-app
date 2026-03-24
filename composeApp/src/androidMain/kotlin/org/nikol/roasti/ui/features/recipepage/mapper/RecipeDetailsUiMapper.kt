package org.nikol.roasti.ui.features.recipepage.mapper

import org.nikol.roasti.feature.recipe.domain.model.Recipe
import org.nikol.roasti.feature.recipe.domain.model.BrewStep
import org.nikol.roasti.feature.recipe.domain.model.RoastLevel
import org.nikol.roasti.ui.features.recipe.mapper.labelRes
import org.nikol.roasti.ui.features.recipe.mapper.toUiModel
import org.nikol.roasti.ui.features.recipepage.model.RecipeDetailsUiModel
import org.nikol.roasti.ui.features.recipepage.model.RecipeStepUiModel
import org.nikol.roasti.core.utils.imageUrl

internal fun Recipe.toUiModel() = RecipeDetailsUiModel(
    id = id,
    title = title,
    description = description,
    note = note,
    imageUrl = imageId?.let(::imageUrl),
    brewMethodLabelRes = brewMethod.labelRes(),
    difficultyLabelRes = difficulty.labelRes(),
    roastLevelLabelRes = roastLevel.takeUnless { it == RoastLevel.NONE }?.labelRes(),
    beans = beans,
    steps = steps.map(BrewStep::toUiModel),
    isLiked = isLiked,
    likesCount = likesCount,
    author = author?.toUiModel(),
    origin = origin?.toUiModel(),
    isPublic = isPublic,
)

private fun BrewStep.toUiModel() = RecipeStepUiModel(
    order = order,
    title = title,
    description = description,
    durationSeconds = durationSeconds,
)
