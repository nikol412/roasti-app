package org.nikol.roasti.ui.features.recipepage.mapper

import org.nikol.roasti.feature.recipe.domain.model.Recipe
import org.nikol.roasti.feature.recipe.domain.model.BrewStep
import org.nikol.roasti.feature.recipe.domain.model.RoastLevel
import org.nikol.roasti.ui.features.recipe.mapper.labelRes
import org.nikol.roasti.ui.features.recipepage.model.RecipeDetailsUiModel
import org.nikol.roasti.ui.features.recipepage.model.RecipeStepUiModel
import org.nikol.roasti.core.utils.imageUrl

internal fun Recipe.toUiModel() = RecipeDetailsUiModel(
    id = id,
    title = title,
    description = description,
    imageUrl = imageId?.let(::imageUrl),
    brewMethodLabelRes = brewMethod.labelRes(),
    difficultyLabelRes = difficulty.labelRes(),
    roastLevelLabelRes = roastLevel.takeUnless { it == RoastLevel.NONE }?.labelRes(),
    beans = beans,
    steps = steps.map(BrewStep::toUiModel),
    isLiked = isLiked,
    likesCount = likesCount,
)

private fun BrewStep.toUiModel() = RecipeStepUiModel(
    order = order,
    title = title,
    description = description,
    durationSeconds = durationSeconds,
)
