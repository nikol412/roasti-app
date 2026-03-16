package org.nikol.roasti.data.upload.mapper

import org.nikol.roasti.data.upload.remote.model.response.ImageUploadResponseDto
import org.nikol.roasti.domain.upload.UploadedImage

fun ImageUploadResponseDto.toDomain() = UploadedImage(id = id)
