package org.nikol.roasti.features.comments

import org.nikol.roasti.features.users.UserId
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@JvmInline
value class CommentId(val value: Uuid)

data class CommentAuthor(
    val id: UserId,
    val username: String,
    val name: String?,
    val avatarId: String?,
)

@OptIn(ExperimentalUuidApi::class)
data class Comment(
    val id: CommentId,
    val isDeleted: Boolean,
    val author: CommentAuthor?,
    val text: String,
    val parentId: CommentId?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class CommentThread(
    val root: Comment,
    val replies: List<Comment>,
)
