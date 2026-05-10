package org.nikol.roasti.features.recipes

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.nikol.roasti.features.common.ApiError

enum class RecipeErrorCode { NOT_FOUND, FORBIDDEN }

@Serializable
data class RecipeError(
    @SerialName("code") override val code: RecipeErrorCode,
    @SerialName("message") override val message: String,
) : ApiError

fun RecipeErrorCode.toError() = when (this) {
    RecipeErrorCode.NOT_FOUND -> RecipeError(this, "Recipe not found")
    RecipeErrorCode.FORBIDDEN -> RecipeError(this, "Forbidden")
}

fun RecipeErrorCode.toHttpStatus() = when (this) {
    RecipeErrorCode.NOT_FOUND -> HttpStatusCode.NotFound
    RecipeErrorCode.FORBIDDEN -> HttpStatusCode.Forbidden
}
