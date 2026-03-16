package org.nikol.roasti.data.recipe.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class DifficultyDto {
    @SerialName("easy")
    EASY,

    @SerialName("medium")
    MEDIUM,

    @SerialName("hard")
    HARD,
}
