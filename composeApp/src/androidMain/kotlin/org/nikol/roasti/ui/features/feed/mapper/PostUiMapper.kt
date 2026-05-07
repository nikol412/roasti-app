package org.nikol.roasti.ui.features.feed.mapper

import org.nikol.roasti.core.utils.imageUrl
import org.nikol.roasti.feature.post.domain.model.Post
import org.nikol.roasti.feature.post.domain.model.VoteDirection
import org.nikol.roasti.ui.features.feed.model.PostUiModel
import org.nikol.roasti.ui.uikit.post.PostRatingStateUi
import org.nikol.roasti.ui.uikit.post.PostUserReaction

fun Post.toUiModel(currentUserId: String? = null): PostUiModel {
    val (title, body) = splitTitleAndBody(text)
    return PostUiModel(
        id = id,
        authorId = author.id,
        authorName = author.name,
        authorImageUrl = author.imageId?.let(::imageUrl),
        postedAt = createdAt,
        title = title,
        body = body,
        postImageUrl = images.firstOrNull()?.let(::imageUrl),
        ratingState = PostRatingStateUi(
            userReaction = userVote.toUi(),
            postRating = rating,
        ),
        commentsCount = commentsCount,
        isOwn = currentUserId != null && author.id == currentUserId,
    )
}

fun VoteDirection.toUi(): PostUserReaction = when (this) {
    VoteDirection.UP -> PostUserReaction.UP
    VoteDirection.DOWN -> PostUserReaction.DOWN
    VoteDirection.NONE -> PostUserReaction.NONE
}

fun PostUserReaction.toDomain(): VoteDirection = when (this) {
    PostUserReaction.UP -> VoteDirection.UP
    PostUserReaction.DOWN -> VoteDirection.DOWN
    PostUserReaction.NONE -> VoteDirection.NONE
}

internal fun splitTitleAndBody(text: String): Pair<String, String?> {
    val trimmed = text.trim()
    val newlineIndex = trimmed.indexOf('\n')
    if (newlineIndex < 0) return trimmed to null
    val title = trimmed.substring(0, newlineIndex).trim()
    val body = trimmed.substring(newlineIndex + 1).trim().takeIf { it.isNotEmpty() }
    return title to body
}
