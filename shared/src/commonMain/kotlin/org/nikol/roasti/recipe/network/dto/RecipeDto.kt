package org.nikol.roasti.recipe.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecipeDto(
    @SerialName("id")
    val id: String,
    @SerialName("author_id")
    val authorId: String,
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("brew_method")
    val brewMethod: BrewMethodDto,
    @SerialName("difficulty")
    val difficulty: DifficultyDto,
    @SerialName("roast_level")
    val roastLevel: RoastLevelDto? = null,
    @SerialName("beans")
    val beans: String? = null,
    @SerialName("steps")
    val steps: List<BrewStepDto>? = null,
)
