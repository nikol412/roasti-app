package org.nikol.roasti.feature.likes.data

import org.nikol.roasti.feature.likes.domain.RecipeLike

fun RecipeLikeDto.toDomain() = RecipeLike(isLiked, likesCount)