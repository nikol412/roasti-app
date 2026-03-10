package org.nikol.roasti.recipe.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecipesResponseDto(
    @SerialName("items")
    val items: List<RecipeDto>,
    @SerialName("page")
    val page: Int,
    @SerialName("limit")
    val limit: Int,
    @SerialName("total_count")
    val totalCount: Int,
)
