package org.nikol.roasti.ui.features.recipelist.mapper

import org.nikol.roasti.core.utils.imageUrl
import org.nikol.roasti.feature.recipe.domain.model.Recipe
import org.nikol.roasti.ui.features.recipe.mapper.labelRes
import org.nikol.roasti.ui.features.recipe.mapper.toUiModel
import org.nikol.roasti.ui.features.recipe.model.RecipeAuthorUiModel
import org.nikol.roasti.ui.features.recipelist.model.RecipeListItemUiModel

internal fun Recipe.toUiModel() = RecipeListItemUiModel(
    id = id,
    title = title,
    description = description,
    note = note,
    imageUrl = imageId?.let(::imageUrl),
    brewMethodLabelRes = brewMethod.labelRes(),
    difficultyLabelRes = difficulty.labelRes(),
    isLiked = isLiked,
    likesCount = likesCount,
    author = author?.toUiModel(),
    origin = origin?.toUiModel(),
    isPublic = isPublic,
)

internal fun org.nikol.roasti.Recipe.toUiModel() = RecipeListItemUiModel(
    id = id,
    title = title,
    description = description,
    imageUrl = image_id?.let(::imageUrl),
    brewMethodLabelRes = brew_method.labelRes(),
    difficultyLabelRes = difficulty.labelRes(),
    isLiked = is_liked == 1L,
    likesCount = likes_count.toInt(),
    author = if (author_id != null && author_name != null) {
        RecipeAuthorUiModel(
            id = author_id!!,
            username = author_name!!,
            avatarId = author_image_id?.let(::imageUrl)
        )
    } else {
        null
    },
)

internal fun org.nikol.roasti.FavoriteRecipe.toUiModel() = RecipeListItemUiModel(
    id = id,
    title = title,
    description = description,
    imageUrl = image_id?.let(::imageUrl),
    brewMethodLabelRes = brew_method.labelRes(),
    difficultyLabelRes = difficulty.labelRes(),
    isLiked = true,  // everything in favorites is liked
    likesCount = likes_count.toInt(),
    author = if (author_id != null && author_name != null) {
        RecipeAuthorUiModel(
            id = author_id!!,
            username = author_name!!,
            avatarId = author_image_id?.let(::imageUrl)
        )
    } else {
        null
    },
)
