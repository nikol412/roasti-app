package org.nikol.roasti.features.users

private val usernameRegex = Regex("^[a-zA-Z0-9_]+$")
private const val USERNAME_MIN_LENGTH = 6
private const val USERNAME_MAX_LENGTH = 16

interface UserService {
    suspend fun getById(id: UserId): Result<User>
    suspend fun getByUsername(username: String): Result<User>
    suspend fun updateProfile(id: UserId, fields: UpdateUserFields): Result<User>
    suspend fun checkUsernameAvailability(username: String): Boolean
    fun validateUsername(username: String): Throwable?
}

class UserServiceImpl(private val repo: UserRepository) : UserService {

    override suspend fun getById(id: UserId): Result<User> =
        repo.findById(id)?.let { Result.success(it) } ?: Result.failure(UserNotFoundException)

    override suspend fun getByUsername(username: String): Result<User> =
        repo.findByUsername(username)?.let { Result.success(it) } ?: Result.failure(UserNotFoundException)

    override suspend fun updateProfile(id: UserId, fields: UpdateUserFields): Result<User> {
        if (fields.username != null) {
            validateUsername(fields.username)?.let { return Result.failure(it) }
            if (repo.existsByUsername(fields.username)) return Result.failure(UsernameTakenException)
        }
        repo.update(id, fields)
        return getById(id)
    }

    override suspend fun checkUsernameAvailability(username: String): Boolean =
        !repo.existsByUsername(username)

    override fun validateUsername(username: String): Throwable? {
        if (username.length < USERNAME_MIN_LENGTH)
            return IllegalArgumentException("username must be at least $USERNAME_MIN_LENGTH characters")
        if (username.length > USERNAME_MAX_LENGTH)
            return IllegalArgumentException("username must be at most $USERNAME_MAX_LENGTH characters")
        if (!usernameRegex.matches(username))
            return IllegalArgumentException("username can only contain letters, numbers and underscores")
        return null
    }
}

val UserNotFoundException = NoSuchElementException("user not found")
val UsernameTakenException = IllegalStateException("username already taken")
val EmailTakenException = IllegalStateException("email already taken")
