package org.nikol.roasti.feature.likes.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import org.nikol.roasti.core.network.ApiRoutes
import org.nikol.roasti.core.network.AuthorizedRequestExecutor

private const val LikedRecipesTypeQueryParameter = "type"
private const val LimitQueryParameter = "limit"
private const val PageQueryParameter = "page"
private const val RecipeLikeType = "recipe"

class LikesApiClient(
    private val httpClient: HttpClient,
    private val authorizedRequestExecutor: AuthorizedRequestExecutor,
) {
    suspend fun getLikedRecipes(
        userId: String,
        limit: Int = 50,
        page: Int = 1
    ): Result<LikedRecipesPageDto> = authorizedRequestExecutor.execute {
        httpClient.get(ApiRoutes.userLikedRecipes(userId)) {
            url {
                parameters.append(LikedRecipesTypeQueryParameter, RecipeLikeType)
                parameters.append(LimitQueryParameter, limit.toString())
                parameters.append(PageQueryParameter, page.toString())
            }
        }.body<LikedRecipesPageDto>()
    }

    suspend fun toggleLikeOnRecipe(recipeId: String): Result<RecipeLikeDto> =
        authorizedRequestExecutor.execute {
            return@execute httpClient.post(ApiRoutes.recipeLike(recipeId)).body<RecipeLikeDto>()
        }
}
