package org.nikol.roasti.features.comments

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.nikol.roasti.features.common.ApiError

enum class CommentErrorCode { NOT_FOUND, FORBIDDEN }

@Serializable
data class CommentError(
    @SerialName("code") override val code: CommentErrorCode,
    @SerialName("message") override val message: String,
) : ApiError

fun CommentErrorCode.toError() = when (this) {
    CommentErrorCode.NOT_FOUND -> CommentError(this, "Comment not found")
    CommentErrorCode.FORBIDDEN -> CommentError(this, "Forbidden")
}

fun CommentErrorCode.toHttpStatus() = when (this) {
    CommentErrorCode.NOT_FOUND -> HttpStatusCode.NotFound
    CommentErrorCode.FORBIDDEN -> HttpStatusCode.Forbidden
}
