package org.nikol.roasti.core.session.storage

interface TokenStorage {
    suspend fun readTokens(): TokensDto?

    suspend fun writeTokens(tokens: TokensDto)

    suspend fun clearTokens()
}
