package org.nikol.roasti.feature.post.data.remote.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PostsPageResponseDto(
    @SerialName("items")
    val items: List<PostResponseDto>,
    @SerialName("pagination")
    val pagination: PostsPaginationResponseDto,
)
