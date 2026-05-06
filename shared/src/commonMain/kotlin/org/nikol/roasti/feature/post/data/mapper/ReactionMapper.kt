package org.nikol.roasti.feature.post.data.mapper

import org.nikol.roasti.feature.post.data.remote.model.ReactionDto
import org.nikol.roasti.feature.post.domain.model.UserReaction

private const val LIKE = "like"
private const val DISLIKE = "dislike"
private const val NONE = "none"

fun UserReaction.toWireString(): String = when (this) {
    UserReaction.LIKE -> LIKE
    UserReaction.DISLIKE -> DISLIKE
    UserReaction.NONE -> NONE
}

fun String?.toUserReaction(): UserReaction = when (this) {
    LIKE -> UserReaction.LIKE
    DISLIKE -> UserReaction.DISLIKE
    else -> UserReaction.NONE
}

fun UserReaction.toDto(): ReactionDto = when (this) {
    UserReaction.LIKE -> ReactionDto.LIKE
    UserReaction.DISLIKE -> ReactionDto.DISLIKE
    UserReaction.NONE -> ReactionDto.NONE
}
