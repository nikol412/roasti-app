package org.nikol.roasti.domain.upload

interface UploadRepository {
    suspend fun uploadImage(fileName: String, bytes: ByteArray): Result<UploadedImage>
}
