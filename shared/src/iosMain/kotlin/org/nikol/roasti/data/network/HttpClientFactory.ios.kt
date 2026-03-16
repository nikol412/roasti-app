package org.nikol.roasti.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val BaseUrl = "155.212.158.252:9090"

actual fun createHttpClient(): HttpClient = HttpClient(Darwin) {
    defaultRequest {
        host = BaseUrl
        url {
            protocol = URLProtocol.HTTP
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
