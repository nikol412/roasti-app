package org.nikol.roasti.data.upload.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import org.nikol.roasti.data.recipe.network.UserId
import org.nikol.roasti.data.recipe.network.UserIdHeader
import org.nikol.roasti.data.upload.dto.ImageDto
import org.nikol.roasti.data.upload.mapper.toDomain
import org.nikol.roasti.domain.upload.UploadedImage

private const val UploadsPath = "/api/v1/uploads/images"

interface UploadApiClient {
    suspend fun uploadImage(fileName: String, bytes: ByteArray): Result<UploadedImage>
}

class UploadApiClientImpl(
    private val httpClient: HttpClient,
) : UploadApiClient {

    override suspend fun uploadImage(fileName: String, bytes: ByteArray): Result<UploadedImage> =
        runCatching {
            httpClient.post(UploadsPath) {
                header(UserIdHeader, UserId)
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("file", bytes, Headers.build {
                                append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                            })
                        }
                    )
                )
            }.body<ImageDto>().toDomain()
        }
}
