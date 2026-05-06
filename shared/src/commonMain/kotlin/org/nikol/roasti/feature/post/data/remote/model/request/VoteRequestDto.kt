package org.nikol.roasti.feature.post.data.remote.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.nikol.roasti.feature.post.data.remote.model.ReactionDto

@Serializable
data class VoteRequestDto(
    @SerialName("reaction")
    val reaction: ReactionDto,
)
