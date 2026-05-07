package org.nikol.roasti.feature.post.data.mapper

import org.nikol.roasti.feature.post.data.remote.model.response.PostRecipeRefDto
import org.nikol.roasti.feature.post.data.remote.model.response.PostRecipeStatusDto
import org.nikol.roasti.feature.post.domain.model.PostRecipeRef
import org.nikol.roasti.feature.post.domain.model.PostRecipeStatus

private const val AVAILABLE = "available"
private const val UNAVAILABLE = "unavailable"

fun PostRecipeRefDto.toDomain(): PostRecipeRef = PostRecipeRef(
    id = id,
    status = status.toDomain(),
)

fun PostRecipeStatusDto.toDomain(): PostRecipeStatus = when (this) {
    PostRecipeStatusDto.AVAILABLE -> PostRecipeStatus.AVAILABLE
    PostRecipeStatusDto.UNAVAILABLE -> PostRecipeStatus.UNAVAILABLE
}

fun PostRecipeStatus.toWireString(): String = when (this) {
    PostRecipeStatus.AVAILABLE -> AVAILABLE
    PostRecipeStatus.UNAVAILABLE -> UNAVAILABLE
}

fun String?.toPostRecipeStatus(): PostRecipeStatus = when (this) {
    UNAVAILABLE -> PostRecipeStatus.UNAVAILABLE
    else -> PostRecipeStatus.AVAILABLE
}
