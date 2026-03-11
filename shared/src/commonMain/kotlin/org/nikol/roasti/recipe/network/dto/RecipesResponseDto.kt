package org.nikol.roasti.recipe.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.nikol.roasti.recipe.model.RecipesPaginated
import org.nikol.roasti.recipe.network.toDomain

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

fun RecipesResponseDto.toDomain() = RecipesPaginated(
    items = items.map { it.toDomain() },
    page = page,
    limit = limit,
    totalCount = totalCount
)
