package org.nikol.roasti.upload.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import org.nikol.roasti.upload.network.dto.ImageDto

private const val UploadsPath = "/api/v1/uploads/images"
private const val UserIdHeader = "X-User-Id"
private const val UserId = "test-user"

interface UploadApiClient {
    suspend fun uploadImage(fileName: String, bytes: ByteArray): Result<ImageDto>
}

class UploadApiClientImpl(
    private val httpClient: HttpClient,
) : UploadApiClient {

    override suspend fun uploadImage(fileName: String, bytes: ByteArray): Result<ImageDto> =
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
            }.body<ImageDto>()
        }
}
