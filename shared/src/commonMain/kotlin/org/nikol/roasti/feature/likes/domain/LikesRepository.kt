package org.nikol.roasti.feature.likes.domain

interface LikesRepository {
    suspend fun toggleLikeOnRecipe(recipeId: String): Result<RecipeLike>
}