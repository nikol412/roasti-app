package org.nikol.roasti.feature.auth.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.nikol.roasti.feature.auth.data.network.model.response.UserDto
import org.nikol.roasti.core.network.ApiRoutes
import org.nikol.roasti.core.network.AuthorizedRequestExecutor

interface ProfileApiClient {
    suspend fun getMyProfile(): Result<UserDto>
}

class ProfileApiClientImpl(
    private val httpClient: HttpClient,
    private val authorizedRequestExecutor: AuthorizedRequestExecutor,
) : ProfileApiClient {

    override suspend fun getMyProfile(): Result<UserDto> = authorizedRequestExecutor.execute { _ ->
        httpClient.get(ApiRoutes.ProfileMe).body<UserDto>()
    }
}
