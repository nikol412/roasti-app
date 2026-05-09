package org.nikol.roasti.features.users

import kotlin.time.Instant

@JvmInline
value class UserId(val value: String)

data class User(
    val id: UserId,
    val email: String,
    val username: String,
    val name: String?,
    val avatarId: String?,
    val bio: String?,
    val createdAt: Instant,
)
