package org.nikol.roasti.feature.likes.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import org.nikol.roasti.core.network.ApiRoutes
import org.nikol.roasti.core.network.AuthorizedRequestExecutor

class LikesApiClient(
    private val httpClient: HttpClient,
    private val authorizedRequestExecutor: AuthorizedRequestExecutor,
) {

    suspend fun toggleLikeOnRecipe(recipeId: String): Result<RecipeLikeDto> =
        authorizedRequestExecutor.execute {
            return@execute httpClient.post(ApiRoutes.recipeLike(recipeId)).body<RecipeLikeDto>()
        }
}
