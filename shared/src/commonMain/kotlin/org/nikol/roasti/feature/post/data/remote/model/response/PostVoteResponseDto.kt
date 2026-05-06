package org.nikol.roasti.feature.post.data.remote.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PostVoteResponseDto(
    @SerialName("rating")
    val rating: Int,
    @SerialName("user_reaction")
    val userReaction: String? = null,
)
