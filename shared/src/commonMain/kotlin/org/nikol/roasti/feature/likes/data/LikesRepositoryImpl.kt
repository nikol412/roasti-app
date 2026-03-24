package org.nikol.roasti.feature.likes.data

import org.nikol.roasti.feature.likes.domain.LikesRepository
import org.nikol.roasti.feature.likes.domain.RecipeLike

class LikesRepositoryImpl(
    private val httpClient: LikesApiClient,
): LikesRepository {
    override suspend fun toggleLikeOnRecipe(recipeId: String): Result<RecipeLike> {
        return httpClient.toggleLikeOnRecipe(recipeId).map { it.toDomain() }
    }
}