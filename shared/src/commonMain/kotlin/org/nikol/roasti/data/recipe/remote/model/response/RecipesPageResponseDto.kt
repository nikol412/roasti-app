package org.nikol.roasti.data.recipe.remote.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecipesPageResponseDto(
    @SerialName("items")
    val items: List<RecipeResponseDto>,
    @SerialName("pagination")
    val pagination: RecipesPaginationResponseDto,
)
