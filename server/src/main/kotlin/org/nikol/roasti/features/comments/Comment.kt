package org.nikol.roasti.features.comments

import org.nikol.roasti.features.users.UserPreview
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@JvmInline
value class CommentId(val value: Uuid)

@OptIn(ExperimentalUuidApi::class)
data class Comment(
    val id: CommentId,
    val isDeleted: Boolean,
    val author: UserPreview?,
    val text: String,
    val parentId: CommentId?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class CommentThread(
    val root: Comment,
    val replies: List<Comment>,
)
