package org.nikol.roasti.auth.domain.model

sealed interface AuthState {
    data object Initializing : AuthState
    data object Guest : AuthState
    data class Authenticated(val session: UserSession) : AuthState
}
