package org.nikol.roasti.feature.recipe.data.remote.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecipesPaginationResponseDto(
    @SerialName("current_page")
    val currentPage: Int,
    @SerialName("items_count")
    val itemsCount: Int,
    @SerialName("last_page")
    val lastPage: Int,
    @SerialName("next_page")
    val nextPage: Int,
)
