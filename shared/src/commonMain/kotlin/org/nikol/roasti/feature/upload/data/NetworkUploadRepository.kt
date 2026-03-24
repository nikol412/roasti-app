package org.nikol.roasti.feature.upload.data

import org.nikol.roasti.feature.upload.data.mapper.toDomain
import org.nikol.roasti.feature.upload.data.network.UploadApiClient
import org.nikol.roasti.feature.upload.domain.UploadRepository
import org.nikol.roasti.feature.upload.domain.UploadedImage

class NetworkUploadRepository(
    private val apiClient: UploadApiClient,
) : UploadRepository {

    override suspend fun uploadImage(fileName: String, bytes: ByteArray): Result<UploadedImage> =
        apiClient.uploadImage(fileName, bytes).map { it.toDomain() }
}
