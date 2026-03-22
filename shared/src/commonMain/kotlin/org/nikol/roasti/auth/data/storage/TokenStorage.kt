package org.nikol.roasti.auth.data.storage

interface TokenStorage {
    suspend fun readTokens(): TokensDto?

    suspend fun writeTokens(tokens: TokensDto)

    suspend fun clearTokens()
}
