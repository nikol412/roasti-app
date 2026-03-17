package org.nikol.roasti.auth.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import org.nikol.roasti.auth.data.network.model.request.LoginRequestDto
import org.nikol.roasti.auth.data.network.model.request.RefreshRequestDto
import org.nikol.roasti.auth.data.network.model.request.RegisterRequestDto
import org.nikol.roasti.auth.data.network.model.response.AuthResponseDto
import org.nikol.roasti.auth.data.network.model.response.RefreshResponseDto
import org.nikol.roasti.data.network.ApiRoutes
import org.nikol.roasti.data.network.NetworkHeaders

interface AuthApiClient {
    suspend fun login(request: LoginRequestDto): Result<AuthResponseDto>

    suspend fun register(request: RegisterRequestDto): Result<AuthResponseDto>

    suspend fun logout(accessToken: String): Result<Unit>

    suspend fun refresh(refreshToken: String): Result<RefreshResponseDto>
}

class AuthApiClientImpl(
    private val httpClient: HttpClient,
) : AuthApiClient {

    override suspend fun login(request: LoginRequestDto): Result<AuthResponseDto> = runCatching {
        httpClient.post(ApiRoutes.Login) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<AuthResponseDto>()
    }

    override suspend fun register(request: RegisterRequestDto): Result<AuthResponseDto> = runCatching {
        httpClient.post(ApiRoutes.Register) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<AuthResponseDto>()
    }

    override suspend fun logout(accessToken: String): Result<Unit> = runCatching {
        httpClient.post(ApiRoutes.Logout) {
            header(HttpHeaders.Authorization, NetworkHeaders.BearerPrefix + accessToken)
        }
    }.map { Unit }

    override suspend fun refresh(refreshToken: String): Result<RefreshResponseDto> = runCatching {
        httpClient.post(ApiRoutes.Refresh) {
            contentType(ContentType.Application.Json)
            setBody(RefreshRequestDto(refreshToken = refreshToken))
        }.body<RefreshResponseDto>()
    }
}
