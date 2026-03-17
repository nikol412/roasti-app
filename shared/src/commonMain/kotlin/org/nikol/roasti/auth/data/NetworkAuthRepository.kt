package org.nikol.roasti.auth.data

import kotlinx.coroutines.flow.StateFlow
import org.nikol.roasti.auth.data.network.AuthApiClient
import org.nikol.roasti.auth.data.network.ProfileApiClient
import org.nikol.roasti.auth.data.network.mapper.toDomain
import org.nikol.roasti.auth.data.network.model.request.LoginRequestDto
import org.nikol.roasti.auth.data.network.model.request.RegisterRequestDto
import org.nikol.roasti.auth.domain.model.AuthState
import org.nikol.roasti.auth.domain.model.User
import org.nikol.roasti.auth.domain.repository.AuthRepository
import org.nikol.roasti.auth.domain.repository.SessionRepository

class NetworkAuthRepository(
    private val authApiClient: AuthApiClient,
    private val profileApiClient: ProfileApiClient,
    private val sessionRepository: SessionRepository,
) : AuthRepository {

    override val authState: StateFlow<AuthState> = sessionRepository.authState

    override suspend fun bootstrap() {
        sessionRepository.restore()
        if (authState.value is AuthState.Authenticated) {
            syncProfile()
        }
    }

    override suspend fun login(
        username: String,
        password: String,
    ): Result<Unit> {
        val result = authApiClient.login(
            LoginRequestDto(
                password = password,
                username = username,
            )
        )

        return if (result.isSuccess) {
            sessionRepository.saveSession(result.getOrThrow().toDomain())
            Result.success(Unit)
        } else {
            Result.failure(result.exceptionOrNull() ?: IllegalStateException("Login failed"))
        }
    }

    override suspend fun register(
        username: String,
        email: String,
        password: String,
        bio: String?,
        avatarId: String?,
    ): Result<Unit> {
        val result = authApiClient.register(
            RegisterRequestDto(
                avatarId = avatarId,
                bio = bio,
                email = email,
                password = password,
                username = username,
            )
        )

        return if (result.isSuccess) {
            sessionRepository.saveSession(result.getOrThrow().toDomain())
            Result.success(Unit)
        } else {
            Result.failure(result.exceptionOrNull() ?: IllegalStateException("Registration failed"))
        }
    }

    override suspend fun logout() {
        sessionRepository.currentSession()?.accessToken?.let { accessToken ->
            authApiClient.logout(accessToken)
        }
        sessionRepository.clearSession()
    }

    override suspend fun syncProfile(): Result<User> {
        val result = profileApiClient.getMyProfile().map { it.toDomain() }
        if (result.isSuccess) {
            sessionRepository.updateUser(result.getOrThrow())
        }
        return result
    }
}
