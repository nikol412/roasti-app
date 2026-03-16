package org.nikol.roasti.recipe.network

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.nikol.roasti.AppConfig

private const val KtorLogTag = "KtorHttp"


actual fun createHttpClient(): HttpClient = HttpClient(OkHttp) {
    expectSuccess = true

    defaultRequest {
        host = AppConfig.HOST
        port = AppConfig.PORT
        url {
            protocol = URLProtocol.HTTP
        }
        header(UserIdHeader, UserId)
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
