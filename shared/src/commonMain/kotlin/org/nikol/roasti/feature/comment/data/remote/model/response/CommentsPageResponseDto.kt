package org.nikol.roasti.feature.comment.data.remote.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommentsPageResponseDto(
    @SerialName("items")
    val items: List<CommentThreadResponseDto>,
    @SerialName("pagination")
    val pagination: CommentsPaginationResponseDto,
)
