package org.nikol.roasti.feature.auth.data.network.mapper

import org.nikol.roasti.feature.auth.data.network.model.response.AuthResponseDto
import org.nikol.roasti.feature.auth.data.network.model.response.RefreshResponseDto
import org.nikol.roasti.feature.auth.data.network.model.response.UserDto
import org.nikol.roasti.feature.auth.domain.model.User
import org.nikol.roasti.core.session.UserSession

fun UserDto.toDomain(): User = User(
    avatarId = avatarId,
    bio = bio,
    id = id,
    username = username,
    email = email,
)

fun AuthResponseDto.toDomain(): UserSession = UserSession(
    accessToken = accessToken,
    refreshToken = refreshToken,
)

fun RefreshResponseDto.toDomain(): UserSession = UserSession(
    accessToken = accessToken,
    refreshToken = refreshToken,
)
