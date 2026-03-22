package org.nikol.roasti.auth.domain.repository

import kotlinx.coroutines.flow.StateFlow
import org.nikol.roasti.auth.data.session.SessionState
import org.nikol.roasti.auth.domain.model.UserSession

interface SessionRepository {
    val authState: StateFlow<SessionState>

    suspend fun restore()

    suspend fun saveSession(session: UserSession)

    suspend fun clearSession()

    fun currentSession(): UserSession?
}
