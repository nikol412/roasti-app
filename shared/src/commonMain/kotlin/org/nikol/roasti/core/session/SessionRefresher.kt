package org.nikol.roasti.core.session

interface SessionRefresher {
    suspend fun refreshSession(failedAccessToken: String): Result<UserSession>
}
