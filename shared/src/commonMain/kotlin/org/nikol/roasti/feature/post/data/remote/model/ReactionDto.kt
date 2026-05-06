package org.nikol.roasti.feature.post.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ReactionDto {
    @SerialName("like") LIKE,
    @SerialName("dislike") DISLIKE,
    @SerialName("none") NONE,
}
