package org.nikol.roasti.ui.features.feed.mapper

import org.nikol.roasti.core.utils.imageUrl
import org.nikol.roasti.feature.post.domain.model.Post
import org.nikol.roasti.feature.post.domain.model.UserReaction
import org.nikol.roasti.ui.features.feed.model.PostUiModel
import org.nikol.roasti.ui.uikit.post.PostRatingStateUi
import org.nikol.roasti.ui.uikit.post.PostUserReaction

fun Post.toUiModel(): PostUiModel {
    val (title, body) = splitTitleAndBody(text)
    return PostUiModel(
        id = id,
        authorName = author.name,
        authorImageUrl = author.imageId?.let(::imageUrl),
        postedAt = createdAt,
        title = title,
        body = body,
        postImageUrl = photos.firstOrNull()?.let(::imageUrl),
        ratingState = PostRatingStateUi(
            userReaction = userReaction.toUi(),
            postRating = rating,
        ),
        commentsCount = commentsCount,
    )
}

fun UserReaction.toUi(): PostUserReaction = when (this) {
    UserReaction.LIKE -> PostUserReaction.UP
    UserReaction.DISLIKE -> PostUserReaction.DOWN
    UserReaction.NONE -> PostUserReaction.NONE
}

fun PostUserReaction.toDomain(): UserReaction = when (this) {
    PostUserReaction.UP -> UserReaction.LIKE
    PostUserReaction.DOWN -> UserReaction.DISLIKE
    PostUserReaction.NONE -> UserReaction.NONE
}

private fun splitTitleAndBody(text: String): Pair<String, String?> {
    val trimmed = text.trim()
    val newlineIndex = trimmed.indexOf('\n')
    if (newlineIndex < 0) return trimmed to null
    val title = trimmed.substring(0, newlineIndex).trim()
    val body = trimmed.substring(newlineIndex + 1).trim().takeIf { it.isNotEmpty() }
    return title to body
}
