package org.nikol.roasti.upload.network.dto

import org.nikol.roasti.upload.model.UploadedImage

data class ImageDto(
    val id: String,
)

fun ImageDto.toDomain() = UploadedImage(id = id)
fun UploadedImage.toDto() = ImageDto(id)