package org.nikol.roasti.data.recipe.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMessageBuilder
import io.ktor.http.contentType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.serializer
import org.nikol.roasti.data.recipe.dto.BrewMethodDto
import org.nikol.roasti.data.recipe.dto.DifficultyDto
import org.nikol.roasti.data.recipe.dto.RecipeDto
import org.nikol.roasti.data.recipe.dto.RecipesResponseDto
import org.nikol.roasti.data.recipe.mapper.toRequest

private const val RecipesPath = "/api/v1/recipes"
internal const val UserIdHeader = "X-User-Id"
internal const val UserId = "test-user"

interface RecipesApiClient {
    suspend fun getRecipes(
        authorId: String? = null,
        brewMethod: BrewMethodDto? = null,
        difficulty: DifficultyDto? = null,
        limit: Int = 50,
        page: Int = 1
    ): Result<RecipesResponseDto>

    suspend fun addRecipe(recipe: RecipeDto): Result<RecipeDto>
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
    ): Result<RecipesResponseDto> = runCatching {
            httpClient.get(RecipesPath) {
                userIdHeader(UserId)
                contentType(ContentType.Application.Json)
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
        }

    override suspend fun addRecipe(recipe: RecipeDto): Result<RecipeDto> = runCatching {
        val body = recipe.toRequest()
        httpClient.post(RecipesPath) {
            userIdHeader(UserId)
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body<RecipeDto>()
    }
}

fun HttpMessageBuilder.userIdHeader(id: String) = header(UserIdHeader, id)

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T : Enum<T>> getSerialName(value: T): String {
    return serializer<T>().descriptor.getElementName(value.ordinal)
}
