package org.nikol.roasti.ui.features.postdetail.mapper

import org.nikol.roasti.core.utils.imageUrl
import org.nikol.roasti.feature.comment.domain.model.Comment
import org.nikol.roasti.feature.comment.domain.model.CommentThread
import org.nikol.roasti.ui.features.postdetail.model.CommentThreadUiModel
import org.nikol.roasti.ui.features.postdetail.model.CommentUiModel

fun Comment.toUi(currentUserId: String?): CommentUiModel {
    val authorId = author?.id
    return CommentUiModel(
        id = id,
        parentId = parentId,
        isDeleted = isDeleted,
        authorId = authorId,
        authorName = author?.username,
        authorAvatarUrl = author?.avatarId?.let(::imageUrl),
        postedAt = createdAt,
        body = text,
        isOwn = !isDeleted && authorId != null && authorId == currentUserId,
    )
}

fun CommentThread.toUi(currentUserId: String?): CommentThreadUiModel = CommentThreadUiModel(
    root = root.toUi(currentUserId),
    replies = replies.map { it.toUi(currentUserId) },
)
