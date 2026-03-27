package org.nikol.roasti.feature.upload.data.mapper

import org.nikol.roasti.feature.upload.data.remote.model.response.ImageUploadResponseDto
import org.nikol.roasti.feature.upload.domain.UploadedImage

fun ImageUploadResponseDto.toDomain() = UploadedImage(id = id)
