package org.nikol.roasti.feature.likes.domain

interface LikesRepository {
    suspend fun getLikedRecipes(
        userId: String,
        limit: Int = 50,
        page: Int = 1
    ): Result<LikedRecipesPage>

    suspend fun toggleLikeOnRecipe(recipeId: String): Result<RecipeLike>
}
