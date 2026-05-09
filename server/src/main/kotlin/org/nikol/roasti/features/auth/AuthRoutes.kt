package org.nikol.roasti.features.auth

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import org.nikol.roasti.FIREBASE_AUTH
import org.nikol.roasti.FirebasePrincipal
import org.nikol.roasti.feature.auth.data.network.model.request.LoginRequestDto
import org.nikol.roasti.feature.auth.data.network.model.request.RegisterRequestDto
import org.nikol.roasti.feature.auth.data.network.model.response.RefreshResponseDto

@Serializable
data class RefreshRequestBody(@SerialName("refresh_token") val refreshToken: String)

@Serializable
data class LogoutRequestBody(@SerialName("refresh_token") val refreshToken: String)

fun Route.authRoutes() {
    val authService by inject<AuthService>()

    route("/auth") {
        post("/register") {
            val request = call.receive<RegisterRequestDto>()
            val response = authService.register(request)
            call.respond(HttpStatusCode.Created, response)
        }

        post("/login") {
            val request = call.receive<LoginRequestDto>()
            val response = authService.login(request.username, request.password)
            call.respond(response)
        }

        post("/refresh") {
            val body = call.receive<RefreshRequestBody>()
            val response = authService.refresh(body.refreshToken)
            call.respond(response)
        }

        authenticate(FIREBASE_AUTH) {
            post("/logout") {
                val body = call.receive<LogoutRequestBody>()
                authService.logout(body.refreshToken)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
