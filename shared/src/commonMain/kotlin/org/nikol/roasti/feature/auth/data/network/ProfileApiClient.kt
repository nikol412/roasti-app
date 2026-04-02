package org.nikol.roasti.feature.auth.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.nikol.roasti.core.network.ApiRoutes
import org.nikol.roasti.core.network.AuthorizedRequestExecutor
import org.nikol.roasti.feature.auth.data.network.model.request.UpdateProfileRequest
import org.nikol.roasti.feature.auth.data.network.model.response.UserDto

interface ProfileApiClient {
    suspend fun getMyProfile(): Result<UserDto>
    suspend fun updateProfile(updateBody: UpdateProfileRequest): Result<UserDto>
}

class ProfileApiClientImpl(
    private val httpClient: HttpClient,
    private val authorizedRequestExecutor: AuthorizedRequestExecutor,
) : ProfileApiClient {

    override suspend fun getMyProfile(): Result<UserDto> = authorizedRequestExecutor.execute { _ ->
        httpClient.get(ApiRoutes.UsersMe).body<UserDto>()
    }

    override suspend fun updateProfile(updateBody: UpdateProfileRequest) =
        authorizedRequestExecutor.execute {
            httpClient.patch(ApiRoutes.UsersMe) {
                contentType(ContentType.Application.Json)
                setBody(updateBody)
            }.body<UserDto>()
        }
}
