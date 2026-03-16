package org.nikol.roasti.data.recipe.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.nikol.roasti.data.recipe.dto.BrewMethodDto
import org.nikol.roasti.data.recipe.dto.DifficultyDto
import org.nikol.roasti.data.recipe.dto.RoastLevelDto

@Serializable
class UploadRecipeRequestBody(
    @SerialName("title") val title: String,
    @SerialName("beans") val beans: String? = null,
    @SerialName("brew_method") val brewMethod: BrewMethodDto? = null,
    @SerialName("description") val description: String,
    @SerialName("difficulty") val difficulty: DifficultyDto,
    @SerialName("image_id") val imageId: String? = null,
    @SerialName("roast_level") val roastLevel: RoastLevelDto? = null,
    @SerialName("steps") val steps: List<UploadRecipeStepRequestBody>,
)

@Serializable
class UploadRecipeStepRequestBody(
    @SerialName("description") val description: String,
    @SerialName("duration_seconds") val durationSeconds: Int? = null,
    @SerialName("image_id") val imageId: String? = null,
    @SerialName("order") val order: Int,
    @SerialName("title") val title: String,
)
