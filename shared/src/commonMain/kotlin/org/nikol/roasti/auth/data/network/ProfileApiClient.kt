package org.nikol.roasti.auth.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.nikol.roasti.auth.data.network.model.response.UserDto
import org.nikol.roasti.data.network.AuthorizedRequestExecutor
import org.nikol.roasti.data.network.bearerAuthorization

private const val MyProfilePath = "/api/v1/profiles/me"

interface ProfileApiClient {
    suspend fun getMyProfile(): Result<UserDto>
}

class ProfileApiClientImpl(
    private val httpClient: HttpClient,
    private val authorizedRequestExecutor: AuthorizedRequestExecutor,
) : ProfileApiClient {

    override suspend fun getMyProfile(): Result<UserDto> = authorizedRequestExecutor.execute {
        httpClient.get(MyProfilePath) {
            bearerAuthorization(it)
        }.body<UserDto>()
    }
}
