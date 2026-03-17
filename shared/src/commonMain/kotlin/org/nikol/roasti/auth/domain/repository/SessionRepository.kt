package org.nikol.roasti.auth.domain.repository

import kotlinx.coroutines.flow.StateFlow
import org.nikol.roasti.auth.domain.model.AuthState
import org.nikol.roasti.auth.domain.model.User
import org.nikol.roasti.auth.domain.model.UserSession

interface SessionRepository {
    val authState: StateFlow<AuthState>

    suspend fun restore()

    suspend fun saveSession(session: UserSession)

    suspend fun updateUser(user: User)

    suspend fun clearSession()

    fun currentSession(): UserSession?
}
