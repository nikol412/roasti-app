package org.nikol.roasti.ui.features.postdetail.mapper

import org.nikol.roasti.core.utils.imageUrl
import org.nikol.roasti.feature.comment.domain.model.Comment
import org.nikol.roasti.feature.comment.domain.model.CommentThread
import org.nikol.roasti.ui.features.postdetail.model.CommentThreadUiModel
import org.nikol.roasti.ui.features.postdetail.model.CommentUiModel

fun Comment.toUi(): CommentUiModel = CommentUiModel(
    id = id,
    parentId = parentId,
    isDeleted = isDeleted,
    authorName = author?.username,
    authorAvatarUrl = author?.avatarId?.let(::imageUrl),
    postedAt = createdAt,
    body = text,
)

fun CommentThread.toUi(): CommentThreadUiModel = CommentThreadUiModel(
    root = root.toUi(),
    replies = replies.map { it.toUi() },
)
