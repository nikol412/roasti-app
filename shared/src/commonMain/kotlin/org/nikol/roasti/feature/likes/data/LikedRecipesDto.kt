package org.nikol.roasti.feature.likes.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.nikol.roasti.feature.recipe.data.remote.model.response.RecipeResponseDto

@Serializable
data class LikedRecipeItemDto(
    @SerialName("liked_at")
    val likedAt: String,
    @SerialName("recipe")
    val recipe: RecipeResponseDto,
)
