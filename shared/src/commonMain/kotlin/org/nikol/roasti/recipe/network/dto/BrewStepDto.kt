package org.nikol.roasti.recipe.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BrewStepDto(
    @SerialName("order")
    val order: Int,
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String,
    @SerialName("duration_seconds")
    val durationSeconds: Int? = null,
    @SerialName("image_id")
    val imageId: String? = null,
)
