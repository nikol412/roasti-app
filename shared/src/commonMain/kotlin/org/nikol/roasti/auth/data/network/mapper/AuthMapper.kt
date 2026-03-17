package org.nikol.roasti.auth.data.network.mapper

import org.nikol.roasti.auth.data.network.model.response.AuthResponseDto
import org.nikol.roasti.auth.data.network.model.response.RefreshResponseDto
import org.nikol.roasti.auth.data.network.model.response.UserDto
import org.nikol.roasti.auth.domain.model.User
import org.nikol.roasti.auth.domain.model.UserSession

fun UserDto.toDomain(): User = User(
    avatarId = avatarId,
    bio = bio,
    id = id,
    username = username,
)

fun AuthResponseDto.toDomain(): UserSession = UserSession(
    accessToken = accessToken,
    refreshToken = refreshToken,
    user = user.toDomain(),
)

fun RefreshResponseDto.toDomain(currentUser: User): UserSession = UserSession(
    accessToken = accessToken,
    refreshToken = refreshToken,
    user = currentUser,
)
