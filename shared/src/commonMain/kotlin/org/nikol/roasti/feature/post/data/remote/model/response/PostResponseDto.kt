package org.nikol.roasti.feature.post.data.remote.model.response

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PostResponseDto(
    @SerialName("id")
    val id: String,
    @SerialName("author")
    val author: PostAuthorDto,
    @SerialName("text")
    val text: String = "",
    @SerialName("photos")
    val photos: List<String> = emptyList(),
    @SerialName("recipe_id")
    val recipeId: String? = null,
    @SerialName("rating")
    val rating: Int = 0,
    @SerialName("user_reaction")
    val userReaction: String? = null,
    @SerialName("comments_count")
    val commentsCount: Int = 0,
    @SerialName("created_at")
    val createdAt: Instant,
    @SerialName("updated_at")
    val updatedAt: Instant,
)
