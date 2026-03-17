package org.nikol.roasti.ui.features.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.nikol.roasti.auth.domain.repository.AuthRepository
import org.nikol.roasti.ui.features.auth.toAuthUiMessage

private const val EmptyUsernameMessage = "Enter your username."
private const val EmptyPasswordMessage = "Enter your password."

data class LoginFormState(
    val username: String = "",
    val password: String = "",
)

sealed interface LoginUiState {
    data class Content(val form: LoginFormState) : LoginUiState
    data class Loading(val form: LoginFormState) : LoginUiState
    data class Error(
        val form: LoginFormState,
        val message: String,
    ) : LoginUiState
}

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow<LoginUiState>(LoginUiState.Content(LoginFormState()))

    val uiState: StateFlow<LoginUiState> = mutableUiState.asStateFlow()

    fun updateUsername(username: String) {
        updateForm { copy(username = username) }
    }

    fun updatePassword(password: String) {
        updateForm { copy(password = password) }
    }

    fun login() {
        val form = currentForm()
        val validationMessage = validate(form)
        if (validationMessage != null) {
            mutableUiState.value = LoginUiState.Error(form, validationMessage)
            return
        }

        mutableUiState.value = LoginUiState.Loading(form)
        viewModelScope.launch {
            authRepository.login(
                username = form.username.trim(),
                password = form.password,
            ).onFailure {
                mutableUiState.value = LoginUiState.Error(form, it.toAuthUiMessage())
            }
        }
    }

    private fun updateForm(transform: LoginFormState.() -> LoginFormState) {
        mutableUiState.value = LoginUiState.Content(currentForm().transform())
    }

    private fun currentForm(): LoginFormState = when (val state = mutableUiState.value) {
        is LoginUiState.Content -> state.form
        is LoginUiState.Error -> state.form
        is LoginUiState.Loading -> state.form
    }

    private fun validate(form: LoginFormState): String? = when {
        form.username.isBlank() -> EmptyUsernameMessage
        form.password.isBlank() -> EmptyPasswordMessage
        else -> null
    }
}
