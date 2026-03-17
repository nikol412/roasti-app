package org.nikol.roasti.ui.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.nikol.roasti.auth.domain.model.AuthState
import org.nikol.roasti.auth.domain.model.User
import org.nikol.roasti.auth.domain.repository.AuthRepository
import org.nikol.roasti.auth.domain.repository.SessionRepository

private const val SessionExpiredMessage = "Your session has ended. Please sign in again."

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Error(val message: String) : ProfileUiState
    data class Content(
        val user: User,
        val isRefreshing: Boolean,
        val isLoggingOut: Boolean,
    ) : ProfileUiState
}

class ProfileViewModel(
    private val authRepository: AuthRepository,
    sessionRepository: SessionRepository,
) : ViewModel() {

    private val isRefreshing = MutableStateFlow(false)
    private val isLoggingOut = MutableStateFlow(false)

    val uiState: StateFlow<ProfileUiState> = combine(
        sessionRepository.authState,
        isRefreshing,
        isLoggingOut,
    ) { authState, refreshing, loggingOut ->
        when (authState) {
            AuthState.Initializing -> ProfileUiState.Loading
            AuthState.Guest -> ProfileUiState.Error(SessionExpiredMessage)
            is AuthState.Authenticated -> ProfileUiState.Content(
                user = authState.session.user,
                isRefreshing = refreshing,
                isLoggingOut = loggingOut,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileUiState.Loading,
    )

    fun syncProfile() {
        if (isRefreshing.value) {
            return
        }

        viewModelScope.launch {
            isRefreshing.value = true
            authRepository.syncProfile()
            isRefreshing.value = false
        }
    }

    fun logout() {
        if (isLoggingOut.value) {
            return
        }

        viewModelScope.launch {
            isLoggingOut.value = true
            authRepository.logout()
            isLoggingOut.value = false
        }
    }
}
