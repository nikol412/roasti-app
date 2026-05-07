package org.nikol.roasti.feature.post.data.remote.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.nikol.roasti.feature.post.data.remote.model.VoteDirectionDto

@Serializable
data class PostVoteResponseDto(
    @SerialName("rating")
    val rating: Int,
    @SerialName("user_vote")
    val userVote: VoteDirectionDto,
)
