package org.nikol.roasti.features.posts

import org.nikol.roasti.features.users.UserId
import org.nikol.roasti.features.votes.VoteDirection
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@JvmInline
value class PostId(val value: Uuid)

data class PostAuthor(
    val id: UserId,
    val username: String,
    val name: String?,
    val avatarId: String?,
)

@OptIn(ExperimentalUuidApi::class)
data class Post(
    val id: PostId,
    val author: PostAuthor,
    val title: String?,
    val text: String?,
    val images: List<String>,
    val recipeId: Uuid?,
    val rating: Int,
    val userVote: VoteDirection,
    val commentsCount: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
)

