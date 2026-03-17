package org.nikol.roasti.auth.storage

import android.content.Context
import kotlinx.serialization.json.Json
import org.nikol.roasti.auth.data.storage.TokenStorage
import org.nikol.roasti.auth.domain.model.UserSession

private const val PreferencesName = "roasti_auth"
private const val SessionKey = "session"

class SharedPreferencesTokenStorage(
    context: Context,
) : TokenStorage {

    private val preferences = context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    override suspend fun readSession(): UserSession? {
        val rawSession = preferences.getString(SessionKey, null) ?: return null
        return runCatching { json.decodeFromString<UserSession>(rawSession) }.getOrNull()
    }

    override suspend fun writeSession(session: UserSession) {
        preferences.edit()
            .putString(SessionKey, json.encodeToString(UserSession.serializer(), session))
            .apply()
    }

    override suspend fun clearSession() {
        preferences.edit()
            .remove(SessionKey)
            .apply()
    }
}
