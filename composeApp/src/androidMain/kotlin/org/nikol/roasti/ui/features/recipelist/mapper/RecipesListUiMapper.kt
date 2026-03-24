package org.nikol.roasti.ui.features.recipelist.mapper

import org.nikol.roasti.feature.recipe.domain.model.Recipe
import org.nikol.roasti.ui.features.recipe.mapper.labelRes
import org.nikol.roasti.ui.features.recipelist.model.RecipeListItemUiModel
import org.nikol.roasti.core.utils.imageUrl

internal fun Recipe.toUiModel() = RecipeListItemUiModel(
    id = id,
    title = title,
    description = description,
    imageUrl = imageId?.let(::imageUrl),
    brewMethodLabelRes = brewMethod.labelRes(),
    difficultyLabelRes = difficulty.labelRes(),
    isLiked = isLiked,
    likesCount = likesCount,
)
