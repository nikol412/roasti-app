package org.nikol.roasti.feature.likes.data

import kotlinx.coroutines.flow.firstOrNull
import org.nikol.roasti.feature.auth.domain.repository.AuthRepository
import org.nikol.roasti.feature.likes.domain.LikesRepository
import org.nikol.roasti.core.domain.Page
import org.nikol.roasti.feature.likes.domain.LikedRecipe
import org.nikol.roasti.feature.likes.domain.RecipeLike

class LikesRepositoryImpl(
    private val httpClient: LikesApiClient,
    private val authRepository: AuthRepository,
): LikesRepository {

    override suspend fun getUserLikedRecipes(limit: Int, page: Int): Result<Page<LikedRecipe>> {
        val userId = authRepository.getUser().firstOrNull()?.id ?: return Result.failure(Throwable("User not found"))
        return httpClient.getLikedRecipes(
            userId = userId,
            limit = limit,
            page = page,
        ).map { it.toDomain() }
    }

    override suspend fun getLikedRecipes(
        userId: String,
        limit: Int,
        page: Int
    ): Result<Page<LikedRecipe>> {
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
