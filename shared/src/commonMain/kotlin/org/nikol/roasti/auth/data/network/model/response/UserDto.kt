package org.nikol.roasti.auth.data.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    @SerialName("avatar_id")
    val avatarId: String? = null,
    @SerialName("bio")
    val bio: String? = null,
    @SerialName("id")
    val id: String,
    @SerialName("username")
    val username: String,
)
