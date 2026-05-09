package org.nikol.roasti.features.users

import kotlin.time.Instant

data class User(
    val id: String,
    val email: String,
    val username: String,
    val name: String?,
    val avatarId: String?,
    val bio: String?,
    val createdAt: Instant,
)
