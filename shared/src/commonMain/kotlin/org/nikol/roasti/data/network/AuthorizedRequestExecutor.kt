package org.nikol.roasti.data.network

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMessageBuilder
import io.ktor.http.HttpStatusCode
import org.nikol.roasti.auth.domain.repository.SessionRepository
import org.nikol.roasti.auth.domain.session.SessionRefresher

private const val BearerPrefix = "Bearer "

class AuthorizedRequestExecutor(
    private val sessionRepository: SessionRepository,
    private val sessionRefresher: SessionRefresher,
) {

    suspend fun <T> execute(block: suspend (accessToken: String) -> T): Result<T> = runCatching {
        executeOrThrow(block)
    }

    private suspend fun <T> executeOrThrow(block: suspend (accessToken: String) -> T): T {
        val currentSession = sessionRepository.currentSession() ?: error("Authorized request requires session")

        return try {
            block(currentSession.accessToken)
        } catch (error: ClientRequestException) {
            if (error.response.status != HttpStatusCode.Unauthorized) {
                throw error
            }

            val refreshedSession = sessionRefresher.refreshSession(currentSession.accessToken).getOrThrow()
            block(refreshedSession.accessToken)
        }
    }
}

fun HttpMessageBuilder.bearerAuthorization(accessToken: String) {
    header(HttpHeaders.Authorization, BearerPrefix + accessToken)
}
