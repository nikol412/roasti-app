package org.nikol.roasti.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.nikol.roasti.AppConfig

actual fun createHttpClient(
    accessTokenProvider: () -> String?,
): HttpClient = HttpClient(Darwin) {
    expectSuccess = true

    defaultRequest {
        host = AppConfig.HOST
        port = AppConfig.PORT
        url {
            protocol = URLProtocol.HTTP
        }
        if (!url.encodedPath.startsWith(ApiRoutes.AuthPathPrefix)) {
            accessTokenProvider()?.let { accessToken ->
                header(HttpHeaders.Authorization, NetworkHeaders.BearerPrefix + accessToken)
            }
        }
    }
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            },
        )
    }
}
