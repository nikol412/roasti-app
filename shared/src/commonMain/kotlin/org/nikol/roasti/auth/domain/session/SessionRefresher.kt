package org.nikol.roasti.auth.domain.session

import org.nikol.roasti.auth.domain.model.UserSession

interface SessionRefresher {
    suspend fun refreshSession(failedAccessToken: String): Result<UserSession>
}
