package org.nikol.roasti.auth.data.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDto(
    @SerialName("avatar_id")
    val avatarId: String? = null,
    @SerialName("bio")
    val bio: String? = null,
    @SerialName("email")
    val email: String,
    @SerialName("password")
    val password: String,
    @SerialName("username")
    val username: String,
)
