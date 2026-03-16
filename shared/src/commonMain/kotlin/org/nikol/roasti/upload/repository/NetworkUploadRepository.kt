package org.nikol.roasti.upload.repository

import org.nikol.roasti.upload.model.UploadedImage
import org.nikol.roasti.upload.network.UploadApiClient
import org.nikol.roasti.upload.network.dto.ImageDto
import org.nikol.roasti.upload.network.dto.toDomain
import org.nikol.roasti.upload.network.dto.toDto

class NetworkUploadRepository(
    private val apiClient: UploadApiClient,
) : UploadRepository {

    override suspend fun uploadImage(fileName: String, bytes: ByteArray): Result<ImageDto> =
        apiClient.uploadImage(fileName, bytes).map { it.toDto() }
}
