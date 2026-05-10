package org.nikol.roasti.features.users

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.route
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import org.nikol.roasti.feature.auth.data.network.model.request.UpdateProfileRequest
import org.nikol.roasti.feature.auth.data.network.model.response.UserDto
import org.nikol.roasti.FIREBASE_AUTH
import org.nikol.roasti.FirebasePrincipal
import org.nikol.roasti.common.api.respondError

@Serializable
data class UsernameAvailabilityResponse(
    @SerialName("available") val available: Boolean,
)

fun Route.userRoutes() {
    val userService by inject<UserService>()

    route("/users") {
        authenticate(FIREBASE_AUTH) {
            get("/me") {
                val userId = call.principal<FirebasePrincipal>()!!.uid
                userService.getById(userId).fold(
                    ifLeft = { call.respondError(it.toHttpStatus(), it.toError()) },
                    ifRight = { call.respond(it.toDto()) },
                )
            }

            patch("/me") {
                val userId = call.principal<FirebasePrincipal>()!!.uid
                val body = call.receive<UpdateProfileRequest>()
                val fields = UpdateUserFields(
                    username = body.username,
                    name = body.name,
                    bio = body.bio,
                    avatarId = body.imageId,
                )
                userService.updateProfile(userId, fields).fold(
                    ifLeft = { call.respondError(it.toHttpStatus(), it.toError()) },
                    ifRight = { call.respond(it.toDto()) },
                )
            }
        }

        get("/username-availability") {
            val username = call.request.queryParameters["username"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "username query param required")
            call.respond(UsernameAvailabilityResponse(userService.checkUsernameAvailability(username)))
        }

        get("/{username}") {
            val username = call.parameters["username"]!!
            userService.getByUsername(username).fold(
                ifLeft = { call.respondError(it.toHttpStatus(), it.toError()) },
                ifRight = { call.respond(it.toDto()) },
            )
        }
    }
}

fun User.toDto() = UserDto(
    id = id.value,
    email = email,
    username = username,
    name = name,
    avatarId = avatarId,
    bio = bio,
)
