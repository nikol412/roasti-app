package org.nikol.roasti.features.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserRecord
import com.google.firebase.auth.AuthErrorCode
import org.nikol.roasti.features.users.CreateUserInput
import org.nikol.roasti.features.users.EmailTakenException
import org.nikol.roasti.features.users.UserRepository
import org.nikol.roasti.features.users.UserService
import org.nikol.roasti.features.users.UsernameTakenException
import org.nikol.roasti.feature.auth.data.network.model.request.RegisterRequestDto
import org.nikol.roasti.feature.auth.data.network.model.response.AuthResponseDto
import org.nikol.roasti.feature.auth.data.network.model.response.RefreshResponseDto
import org.nikol.roasti.feature.auth.data.network.model.response.UserDto
import org.nikol.roasti.features.users.toDto

private const val PASSWORD_MIN_LENGTH = 8
private const val PASSWORD_MAX_LENGTH = 32

interface AuthService {
    suspend fun register(request: RegisterRequestDto): AuthResponseDto
    suspend fun login(username: String, password: String): AuthResponseDto
    suspend fun refresh(refreshToken: String): RefreshResponseDto
    suspend fun logout(refreshToken: String)
}

class AuthServiceImpl(
    private val userRepo: UserRepository,
    private val userService: UserService,
    private val signer: FirebaseSigner,
    private val revokedTokens: RevokedTokenRepository,
    private val firebaseAuth: FirebaseAuth,
) : AuthService {

    override suspend fun register(request: RegisterRequestDto): AuthResponseDto {
        validatePassword(request.password)
        userService.validateUsername(request.username)
            ?.let { throw IllegalArgumentException(it.message) }

        if (userRepo.existsByUsername(request.username)) throw UsernameTakenException
        if (userRepo.existsByEmail(request.email)) throw EmailTakenException

        val firebaseUser = try {
            firebaseAuth.createUser(
                UserRecord.CreateRequest()
                    .setEmail(request.email)
                    .setPassword(request.password)
            )
        } catch (e: FirebaseAuthException) {
            throw when (e.authErrorCode) {
                AuthErrorCode.EMAIL_ALREADY_EXISTS -> EmailTakenException
                else -> e
            }
        }

        val user = userRepo.create(
            CreateUserInput(
                id = firebaseUser.uid,
                email = request.email,
                username = request.username,
                name = request.name,
                avatarId = request.avatarId,
                bio = request.bio,
            )
        )

        val tokens = signer.signInWithPassword(request.email, request.password)
        return AuthResponseDto(
            accessToken = tokens.idToken,
            refreshToken = tokens.refreshToken,
            user = user.toDto(),
        )
    }

    override suspend fun login(username: String, password: String): AuthResponseDto {
        val user = userRepo.findByUsername(username)
            ?: throw AuthException.InvalidCredentials

        val tokens = signer.signInWithPassword(user.email, password)
        return AuthResponseDto(
            accessToken = tokens.idToken,
            refreshToken = tokens.refreshToken,
            user = user.toDto(),
        )
    }

    override suspend fun refresh(refreshToken: String): RefreshResponseDto {
        if (revokedTokens.isRevoked(refreshToken)) throw AuthException.TokenRevoked

        val tokens = signer.refreshToken(refreshToken)
        return RefreshResponseDto(
            accessToken = tokens.idToken,
            refreshToken = tokens.refreshToken,
        )
    }

    override suspend fun logout(refreshToken: String) {
        revokedTokens.add(refreshToken)
    }

    private fun validatePassword(password: String) {
        require(password.length >= PASSWORD_MIN_LENGTH) {
            "password must be at least $PASSWORD_MIN_LENGTH characters"
        }
        require(password.length <= PASSWORD_MAX_LENGTH) {
            "password must be at most $PASSWORD_MAX_LENGTH characters"
        }
    }
}
