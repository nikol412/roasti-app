package org.nikol.roasti

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.nikol.roasti.feature.auth.data.network.model.request.RegisterRequestDto
import org.nikol.roasti.feature.auth.data.network.model.response.AuthResponseDto
import java.util.UUID

private val testJson = Json { ignoreUnknownKeys = true; explicitNulls = false }

fun withApp(block: suspend ApplicationTestBuilder.() -> Unit) = testApplication {
    environment {
        config = ApplicationConfig("application.conf")
    }
    block()
}

fun ApplicationTestBuilder.jsonClient(token: String? = null): HttpClient = createClient {
    install(ContentNegotiation) { json(testJson) }
    if (token != null) defaultRequest { bearerAuth(token) }
}

suspend fun ApplicationTestBuilder.newAuthenticatedClient(): HttpClient {
    val email = "${UUID.randomUUID()}@test.com"
    val username = "user_${UUID.randomUUID().toString().take(8)}"

    val auth = jsonClient().post("/api/v1/auth/register") {
        contentType(ContentType.Application.Json)
        setBody(RegisterRequestDto(email = email, password = "password123", username = username))
    }.body<AuthResponseDto>()

    return jsonClient(token = auth.accessToken)
}
