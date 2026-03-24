package org.nikol.roasti.feature.likes.data

import org.nikol.roasti.feature.likes.domain.LikesRepository
import org.nikol.roasti.feature.likes.domain.LikedRecipesPage
import org.nikol.roasti.feature.likes.domain.RecipeLike

class LikesRepositoryImpl(
    private val httpClient: LikesApiClient,
): LikesRepository {
    override suspend fun getLikedRecipes(
        userId: String,
        limit: Int,
        page: Int
    ): Result<LikedRecipesPage> {
        return httpClient.getLikedRecipes(
            userId = userId,
            limit = limit,
            page = page,
        ).map { it.toDomain() }
    }

    override suspend fun toggleLikeOnRecipe(recipeId: String): Result<RecipeLike> {
        return httpClient.toggleLikeOnRecipe(recipeId).map { it.toDomain() }
    }
}
