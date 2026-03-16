package org.nikol.roasti.data.upload

import org.nikol.roasti.data.upload.network.UploadApiClient
import org.nikol.roasti.domain.upload.UploadRepository
import org.nikol.roasti.domain.upload.UploadedImage

class NetworkUploadRepository(
    private val apiClient: UploadApiClient,
) : UploadRepository {

    override suspend fun uploadImage(fileName: String, bytes: ByteArray): Result<UploadedImage> =
        apiClient.uploadImage(fileName, bytes)
}
