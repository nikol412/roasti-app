package org.nikol.roasti.feature.post.domain.model

enum class UserReaction {
    LIKE,
    DISLIKE,
    NONE;

    fun deltaTo(target: UserReaction): Int = when (this) {
        LIKE -> when (target) {
            LIKE -> 0
            NONE -> -1
            DISLIKE -> -2
        }
        NONE -> when (target) {
            LIKE -> +1
            NONE -> 0
            DISLIKE -> -1
        }
        DISLIKE -> when (target) {
            LIKE -> +2
            NONE -> +1
            DISLIKE -> 0
        }
    }
}
