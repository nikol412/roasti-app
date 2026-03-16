package org.nikol.roasti.upload.repository

import org.nikol.roasti.upload.model.UploadedImage
import org.nikol.roasti.upload.network.dto.ImageDto

interface UploadRepository {
    suspend fun uploadImage(fileName: String, bytes: ByteArray): Result<ImageDto>
}
