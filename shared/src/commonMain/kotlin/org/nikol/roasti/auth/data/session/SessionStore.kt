package org.nikol.roasti.auth.data.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.nikol.roasti.auth.data.storage.TokenStorage
import org.nikol.roasti.auth.domain.model.AuthState
import org.nikol.roasti.auth.domain.model.User
import org.nikol.roasti.auth.domain.model.UserSession
import org.nikol.roasti.auth.domain.repository.SessionRepository

class SessionStore(
    private val tokenStorage: TokenStorage,
) : SessionRepository {

    private val mutableAuthState = MutableStateFlow<AuthState>(AuthState.Initializing)

    override val authState: StateFlow<AuthState> = mutableAuthState.asStateFlow()

    override suspend fun restore() {
        val session = tokenStorage.readSession()
        mutableAuthState.value = session?.toAuthenticatedState() ?: AuthState.Guest
    }

    override suspend fun saveSession(session: UserSession) {
        tokenStorage.writeSession(session)
        mutableAuthState.value = session.toAuthenticatedState()
    }

    override suspend fun updateUser(user: User) {
        val currentSession = currentSession() ?: return
        saveSession(currentSession.copy(user = user))
    }

    override suspend fun clearSession() {
        tokenStorage.clearSession()
        mutableAuthState.value = AuthState.Guest
    }

    override fun currentSession(): UserSession? = (authState.value as? AuthState.Authenticated)?.session
}

private fun UserSession.toAuthenticatedState(): AuthState.Authenticated = AuthState.Authenticated(this)
