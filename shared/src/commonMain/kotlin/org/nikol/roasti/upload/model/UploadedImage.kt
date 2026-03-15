package org.nikol.roasti.upload.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadedImage(
    @SerialName("id")
    val id: String,
)
