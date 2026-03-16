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
import org.nikol.roasti.data.recipe.remote.model.BrewMethodDto
import org.nikol.roasti.data.recipe.remote.model.DifficultyDto
import org.nikol.roasti.data.recipe.remote.model.request.CreateRecipeRequestDto
import org.nikol.roasti.data.recipe.remote.model.response.RecipeResponseDto
import org.nikol.roasti.data.recipe.remote.model.response.RecipesPageResponseDto

private const val RecipesPath = "/api/v1/recipes"
internal const val UserIdHeader = "X-User-Id"
internal const val UserId = "test-user"

interface RecipesApiClient {
    suspend fun getRecipes(
        authorId: String? = null,
        brewMethod: BrewMethodDto = BrewMethodDto.NONE,
        difficulty: DifficultyDto? = null,
        limit: Int = 50,
        page: Int = 1
    ): Result<RecipesPageResponseDto>

    suspend fun addRecipe(recipe: CreateRecipeRequestDto): Result<RecipeResponseDto>
}

class RecipesApiClientImpl(
    private val httpClient: HttpClient,
) : RecipesApiClient {

    override suspend fun getRecipes(
        authorId: String?,
        brewMethod: BrewMethodDto,
        difficulty: DifficultyDto?,
        limit: Int,
        page: Int
    ): Result<RecipesPageResponseDto> = runCatching {
            httpClient.get(RecipesPath) {
                userIdHeader(UserId)
                contentType(ContentType.Application.Json)
                url {
                    parameters.append("brew_method", getSerialName(brewMethod))
                    difficulty?.let { parameters.append("difficulty", getSerialName(it)) }
                    parameters.append("limit", limit.toString())
                    parameters.append("page", page.toString())
                }
            }.body<RecipesPageResponseDto>()
        }

    override suspend fun addRecipe(recipe: CreateRecipeRequestDto): Result<RecipeResponseDto> = runCatching {
        httpClient.post(RecipesPath) {
            userIdHeader(UserId)
            contentType(ContentType.Application.Json)
            setBody(recipe)
        }.body<RecipeResponseDto>()
    }
}

fun HttpMessageBuilder.userIdHeader(id: String) = header(UserIdHeader, id)

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T : Enum<T>> getSerialName(value: T): String {
    return serializer<T>().descriptor.getElementName(value.ordinal)
}
