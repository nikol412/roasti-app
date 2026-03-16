package org.nikol.roasti.data.upload.mapper

import org.nikol.roasti.data.upload.dto.ImageDto
import org.nikol.roasti.domain.upload.UploadedImage

fun ImageDto.toDomain() = UploadedImage(id = id)
fun UploadedImage.toDto() = ImageDto(id)
