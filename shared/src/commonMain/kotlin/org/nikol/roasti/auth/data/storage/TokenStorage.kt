package org.nikol.roasti.auth.data.storage

import org.nikol.roasti.auth.domain.model.UserSession

interface TokenStorage {
    suspend fun readSession(): UserSession?

    suspend fun writeSession(session: UserSession)

    suspend fun clearSession()
}
