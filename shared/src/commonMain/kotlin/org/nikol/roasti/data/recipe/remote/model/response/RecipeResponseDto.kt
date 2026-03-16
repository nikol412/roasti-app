package org.nikol.roasti.data.recipe.remote.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.nikol.roasti.data.recipe.remote.model.BrewMethodDto
import org.nikol.roasti.data.recipe.remote.model.DifficultyDto
import org.nikol.roasti.data.recipe.remote.model.RoastLevelDto

@Serializable
data class RecipeResponseDto(
    @SerialName("id")
    val id: String,
    @SerialName("author_id")
    val authorId: String,
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String,
    @SerialName("image_id")
    val imageId: String? = null,
    @SerialName("brew_method")
    val brewMethod: BrewMethodDto? = null,
    @SerialName("difficulty")
    val difficulty: DifficultyDto,
    @SerialName("roast_level")
    val roastLevel: RoastLevelDto? = null,
    @SerialName("beans")
    val beans: String? = null,
    @SerialName("steps")
    val steps: List<RecipeStepResponseDto>? = null,
)
