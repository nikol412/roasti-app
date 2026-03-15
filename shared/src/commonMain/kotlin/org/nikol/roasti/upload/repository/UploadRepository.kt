package org.nikol.roasti.upload.repository

import org.nikol.roasti.upload.model.UploadedImage

interface UploadRepository {
    suspend fun uploadImage(fileName: String, bytes: ByteArray): Result<UploadedImage>
}
