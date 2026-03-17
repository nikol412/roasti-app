package org.nikol.roasti.data.upload.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import org.nikol.roasti.data.network.AuthorizedRequestExecutor
import org.nikol.roasti.data.network.bearerAuthorization
import org.nikol.roasti.data.upload.remote.model.response.ImageUploadResponseDto

private const val UploadsPath = "/api/v1/uploads/images"

interface UploadApiClient {
    suspend fun uploadImage(fileName: String, bytes: ByteArray): Result<ImageUploadResponseDto>
}

class UploadApiClientImpl(
    private val httpClient: HttpClient,
    private val authorizedRequestExecutor: AuthorizedRequestExecutor,
) : UploadApiClient {

    override suspend fun uploadImage(fileName: String, bytes: ByteArray): Result<ImageUploadResponseDto> =
        authorizedRequestExecutor.execute { accessToken ->
            httpClient.post(UploadsPath) {
                bearerAuthorization(accessToken)
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("file", bytes, Headers.build {
                                append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                            })
                        }
                    )
                )
            }.body<ImageUploadResponseDto>()
        }
}
