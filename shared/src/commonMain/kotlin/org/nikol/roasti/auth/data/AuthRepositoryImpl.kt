package org.nikol.roasti.auth.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.nikol.roasti.auth.data.local.UserCacheDataSource
import org.nikol.roasti.auth.data.network.AuthApiClient
import org.nikol.roasti.auth.data.network.ProfileApiClient
import org.nikol.roasti.auth.data.network.mapper.toDomain
import org.nikol.roasti.auth.data.network.model.request.LoginRequestDto
import org.nikol.roasti.auth.data.network.model.request.RegisterRequestDto
import org.nikol.roasti.auth.data.network.model.response.AuthResponseDto
import org.nikol.roasti.auth.data.network.model.response.UserDto
import org.nikol.roasti.auth.data.session.SessionState
import org.nikol.roasti.auth.domain.model.AuthState
import org.nikol.roasti.auth.domain.model.User
import org.nikol.roasti.auth.domain.repository.AuthRepository
import org.nikol.roasti.auth.domain.repository.SessionRepository

class AuthRepositoryImpl(
    private val authApiClient: AuthApiClient,
    private val profileApiClient: ProfileApiClient,
    private val sessionRepository: SessionRepository,
    private val userCacheDataSource: UserCacheDataSource,
) : AuthRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val authState: StateFlow<AuthState> = combine(
        sessionRepository.authState,
        userCacheDataSource.getUser(),
    ) { sessionState, cachedUser ->
        when (sessionState) {
            SessionState.Empty -> AuthState.Loading
            SessionState.Guest -> AuthState.Guest
            is SessionState.Error -> AuthState.Error(sessionState.message)
            is SessionState.Authenticated -> when (cachedUser) {
                null -> AuthState.Loading
                else -> AuthState.Authenticated(cachedUser.toDomain())
            }
        }
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = AuthState.Loading,
    )

    override fun getUser(): Flow<User?> = userCacheDataSource.getUser().map { it?.toDomain() }

    override suspend fun bootstrap() {
        sessionRepository.restore()
        if (sessionRepository.authState.value is SessionState.Authenticated) {
            syncProfile()
        }
    }

    override suspend fun login(username: String, password: String): Result<Unit> {
        val result = authApiClient.login(LoginRequestDto(password = password, username = username))
        return if (result.isSuccess) {
            val response = result.getOrThrow()
            saveSession(response)
            saveUser(response.user)
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
            RegisterRequestDto(avatarId = avatarId, bio = bio, email = email, password = password, username = username)
        )
        return if (result.isSuccess) {
            val response = result.getOrThrow()
            saveSession(response)
            saveUser(response.user)
            Result.success(Unit)
        } else {
            Result.failure(result.exceptionOrNull() ?: IllegalStateException("Registration failed"))
        }
    }

    override suspend fun logout() {
        sessionRepository.currentSession()?.accessToken?.let { authApiClient.logout(it) }
        sessionRepository.clearSession()
        userCacheDataSource.deleteUser()
    }

    override suspend fun syncProfile(): Result<User> {
        val result = profileApiClient.getMyProfile()
        result.getOrNull()?.let { saveUser(it) }
        return result.map { it.toDomain() }
    }

    private suspend fun saveUser(user: UserDto) {
        userCacheDataSource.saveUser(
            id = user.id,
            imageId = user.avatarId,
            bio = user.bio,
            username = user.username,
        )
    }

    private suspend fun saveSession(response: AuthResponseDto) {
        sessionRepository.saveSession(response.toDomain())
    }

    private fun org.nikol.roasti.User.toDomain() = User(
        id = id,
        username = username,
        bio = bio,
        avatarId = image_id,
    )
}
