package org.nikol.roasti.feature.likes.data

import org.nikol.roasti.feature.recipe.data.mapper.toDomain
import org.nikol.roasti.feature.likes.domain.RecipeLike
import org.nikol.roasti.feature.likes.domain.LikedRecipe
import org.nikol.roasti.core.domain.Page
import org.nikol.roasti.core.network.PageResponseDto

fun RecipeLikeDto.toDomain() = RecipeLike(isLiked, likesCount)

fun PageResponseDto<LikedRecipeItemDto>.toDomain() = Page(
    items = items.map { it.toDomain() },
    currentPage = pagination.currentPage,
    itemsCount = pagination.itemsCount,
    lastPage = pagination.lastPage,
    nextPage = pagination.nextPage,
)

fun LikedRecipeItemDto.toDomain() = LikedRecipe(
    likedAt = likedAt,
    recipe = recipe.toDomain(),
)
