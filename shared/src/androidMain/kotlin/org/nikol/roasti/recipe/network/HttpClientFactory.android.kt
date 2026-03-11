package org.nikol.roasti.recipe.network

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val BaseUrl = "155.212.158.252:9090"
private const val KtorLogTag = "KtorHttp"

actual fun createHttpClient(): HttpClient = HttpClient(OkHttp) {
    expectSuccess = true

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
    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) {
                Log.d(KtorLogTag, message)
            }
        }
        level = LogLevel.BODY
    }
}
