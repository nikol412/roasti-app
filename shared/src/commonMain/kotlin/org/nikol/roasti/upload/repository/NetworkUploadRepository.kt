package org.nikol.roasti.upload.repository

import org.nikol.roasti.upload.model.UploadedImage
import org.nikol.roasti.upload.network.UploadApiClient
import org.nikol.roasti.upload.network.dto.toDomain

class NetworkUploadRepository(
    private val apiClient: UploadApiClient,
) : UploadRepository {

    override suspend fun uploadImage(fileName: String, bytes: ByteArray): Result<UploadedImage> =
        apiClient.uploadImage(fileName, bytes).map { it.toDomain() }
}
