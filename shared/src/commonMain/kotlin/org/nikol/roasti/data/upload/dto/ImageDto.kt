package org.nikol.roasti.data.upload.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ImageDto(
    @SerialName("id")
    val id: String,
)
