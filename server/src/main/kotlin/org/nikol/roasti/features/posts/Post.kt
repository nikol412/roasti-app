package org.nikol.roasti.features.posts

import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class Post(
    val id: Uuid,
    val authorId: String,
    val text: String,
    val images: List<String>,
    val recipeId: Uuid?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
