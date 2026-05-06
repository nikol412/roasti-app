package org.nikol.roasti.feature.post.domain.model

import kotlinx.datetime.Instant

data class Post(
    val id: String,
    val text: String,
    val photos: List<String>,
    val recipeId: String?,
    val rating: Int,
    val userReaction: UserReaction,
    val commentsCount: Int,
    val author: PostAuthor,
    val createdAt: Instant,
    val updatedAt: Instant,
)
