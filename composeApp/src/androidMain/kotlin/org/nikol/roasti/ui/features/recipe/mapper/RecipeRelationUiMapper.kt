package org.nikol.roasti.ui.features.recipe.mapper

import org.nikol.roasti.feature.recipe.domain.model.Author
import org.nikol.roasti.feature.recipe.domain.model.RecipeOrigin
import org.nikol.roasti.ui.features.recipe.model.RecipeAuthorUiModel
import org.nikol.roasti.ui.features.recipe.model.RecipeOriginUiModel

internal fun Author.toUiModel() = RecipeAuthorUiModel(
    id = id,
    username = username,
    avatarId = avatarId,
)

internal fun RecipeOrigin.toUiModel() = RecipeOriginUiModel(
    recipeId = recipeId,
    author = author.toUiModel(),
)
