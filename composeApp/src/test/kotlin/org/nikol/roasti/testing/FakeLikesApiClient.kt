package org.nikol.roasti.testing

import kotlinx.coroutines.CompletableDeferred
import org.nikol.roasti.feature.likes.data.LikedRecipesPageDto
import org.nikol.roasti.feature.likes.data.LikesApiClient
import org.nikol.roasti.feature.likes.data.RecipeLikeDto

class FakeLikesApiClient : LikesApiClient {

    val pages: MutableMap<Int, Result<LikedRecipesPageDto>> = mutableMapOf()
    var getLikedRecipesCallCount: Int = 0
        private set

    var toggleResult: Result<RecipeLikeDto> = Result.success(RecipeLikeDto(isLiked = true, likesCount = 1))
    var toggleGate: CompletableDeferred<Unit>? = null
    var toggleCallCount: Int = 0
        private set
    val toggledIds: MutableList<String> = mutableListOf()

    override suspend fun getLikedRecipes(
        userId: String,
        limit: Int,
        page: Int,
    ): Result<LikedRecipesPageDto> {
        getLikedRecipesCallCount++
        return pages[page] ?: Result.failure(NoSuchElementException("no fake liked page $page"))
    }

    override suspend fun toggleLikeOnRecipe(recipeId: String): Result<RecipeLikeDto> {
        toggleCallCount++
        toggledIds += recipeId
        toggleGate?.await()
        return toggleResult
    }
}
