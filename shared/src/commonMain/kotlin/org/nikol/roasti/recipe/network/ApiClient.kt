package org.nikol.roasti.recipe.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpMessageBuilder
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.serializer
import org.nikol.roasti.recipe.network.dto.BrewMethodDto
import org.nikol.roasti.recipe.network.dto.DifficultyDto
import org.nikol.roasti.recipe.network.dto.RecipesResponseDto

private const val RecipesPath = "/api/v1/recipes"
private const val UserIdHeader = "X-User-Id"
private const val UserId = "test-user"

interface RecipesApiClient {
    suspend fun getRecipes(
        authorId: String? = null,
        brewMethod: BrewMethodDto? = null,
        difficulty: DifficultyDto? = null,
        limit: Int = 50,
        page: Int = 1
    ): Result<RecipesResponseDto>


}

class RecipesApiClientImpl(
    private val httpClient: HttpClient,
) : RecipesApiClient {

    override suspend fun getRecipes(
        authorId: String?,
        brewMethod: BrewMethodDto?,
        difficulty: DifficultyDto?,
        limit: Int,
        page: Int
    ): Result<RecipesResponseDto> {
        return try {
            val result = httpClient.get(RecipesPath) {
                userIdHeader(UserId)
                url {
                    if (brewMethod != null) parameters.append(
                        "brew_method",
                        getSerialName(brewMethod)
                    )
                    if (difficulty != null) parameters.append(
                        "difficulty",
                        getSerialName(difficulty)
                    )
                    parameters.append("limit", limit.toString())
                    parameters.append("page", page.toString())
                }
            }.body<RecipesResponseDto>()
            Result.success(result)

        } catch (th: Throwable) {
            Result.failure(th)
        }
    }
}

private fun HttpMessageBuilder.userIdHeader(id: String) = header(UserIdHeader, id)

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T : Enum<T>> getSerialName(value: T): String {
    return serializer<T>().descriptor.getElementName(value.ordinal)
}