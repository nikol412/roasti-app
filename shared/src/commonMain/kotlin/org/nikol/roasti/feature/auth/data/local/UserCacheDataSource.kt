package org.nikol.roasti.feature.auth.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.nikol.roasti.User
import org.nikol.roasti.UserQueries

class UserCacheDataSource(private val userQueries: UserQueries) {
    fun getUser(): Flow<User?> = userQueries.getUser().asFlow().mapToOneOrNull(Dispatchers.Default)
    suspend fun saveUser(id: String, imageId: String?, bio: String?, username: String, email: String) =
        withContext(Dispatchers.Default) {
            userQueries.upsertUser(
                id = id,
                image_id = imageId,
                bio = bio,
                username = username,
                email = email,
            )
        }

    suspend fun deleteUser() = withContext(Dispatchers.Default) {
        userQueries.deleteUser()
    }
}