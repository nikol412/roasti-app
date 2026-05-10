package org.nikol.roasti.feature.likes.domain

import org.nikol.roasti.core.domain.Page

interface LikesRepository {

    suspend fun getUserLikedRecipes(limit: Int = 50, page: Int = 1): Result<Page<LikedRecipe>>
    suspend fun getLikedRecipes(
        userId: String,
        limit: Int = 50,
        page: Int = 1
    ): Result<Page<LikedRecipe>>

    suspend fun toggleLikeOnRecipe(recipeId: String): Result<RecipeLike>
}
