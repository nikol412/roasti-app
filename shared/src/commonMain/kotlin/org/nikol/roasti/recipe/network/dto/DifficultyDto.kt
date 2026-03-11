package org.nikol.roasti.recipe.network.dto

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

    @SerialName("none")
    NONE,
}
