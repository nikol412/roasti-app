package org.nikol.roasti.ui.features.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.nikol.roasti.auth.domain.repository.AuthRepository
import org.nikol.roasti.ui.features.auth.toAuthUiMessage

private const val EmptyUsernameMessage = "Choose a username."
private const val EmptyEmailMessage = "Enter your email."
private const val InvalidEmailMessage = "Enter a valid email."
private const val EmptyPasswordMessage = "Create a password."

data class RegisterFormState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val bio: String = "",
)

sealed interface RegisterUiState {
    data class Content(val form: RegisterFormState) : RegisterUiState
    data class Loading(val form: RegisterFormState) : RegisterUiState
    data class Error(
        val form: RegisterFormState,
        val message: String,
    ) : RegisterUiState
}

class RegisterViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Content(RegisterFormState()))

    val uiState: StateFlow<RegisterUiState> = mutableUiState.asStateFlow()

    fun updateUsername(username: String) {
        updateForm { copy(username = username) }
    }

    fun updateEmail(email: String) {
        updateForm { copy(email = email) }
    }

    fun updatePassword(password: String) {
        updateForm { copy(password = password) }
    }

    fun updateBio(bio: String) {
        updateForm { copy(bio = bio) }
    }

    fun register() {
        val form = currentForm()
        val validationMessage = validate(form)
        if (validationMessage != null) {
            mutableUiState.value = RegisterUiState.Error(form, validationMessage)
            return
        }

        mutableUiState.value = RegisterUiState.Loading(form)
        viewModelScope.launch {
            authRepository.register(
                username = form.username.trim(),
                email = form.email.trim(),
                password = form.password,
                bio = form.bio.trim().ifBlank { null },
                avatarId = null,
            ).onFailure {
                mutableUiState.value = RegisterUiState.Error(form, it.toAuthUiMessage())
            }
        }
    }

    private fun updateForm(transform: RegisterFormState.() -> RegisterFormState) {
        mutableUiState.value = RegisterUiState.Content(currentForm().transform())
    }

    private fun currentForm(): RegisterFormState = when (val state = mutableUiState.value) {
        is RegisterUiState.Content -> state.form
        is RegisterUiState.Error -> state.form
        is RegisterUiState.Loading -> state.form
    }

    private fun validate(form: RegisterFormState): String? = when {
        form.username.isBlank() -> EmptyUsernameMessage
        form.email.isBlank() -> EmptyEmailMessage
        !form.email.contains("@") -> InvalidEmailMessage
        form.password.isBlank() -> EmptyPasswordMessage
        else -> null
    }
}
