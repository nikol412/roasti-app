package org.nikol.roasti.data.recipe.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.serializer
import org.nikol.roasti.data.network.ApiRoutes
import org.nikol.roasti.data.network.AuthorizedRequestExecutor
import org.nikol.roasti.data.recipe.remote.model.BrewMethodDto
import org.nikol.roasti.data.recipe.remote.model.DifficultyDto
import org.nikol.roasti.data.recipe.remote.model.request.CreateRecipeRequestDto
import org.nikol.roasti.data.recipe.remote.model.response.RecipeResponseDto
import org.nikol.roasti.data.recipe.remote.model.response.RecipesPageResponseDto

interface RecipesApiClient {
    suspend fun getRecipes(
        authorId: String? = null,
        brewMethod: BrewMethodDto = BrewMethodDto.NONE,
        difficulty: DifficultyDto? = null,
        limit: Int = 50,
        page: Int = 1
    ): Result<RecipesPageResponseDto>

    suspend fun addRecipe(recipe: CreateRecipeRequestDto): Result<RecipeResponseDto>

    suspend fun updateRecipe(id: String, recipe: CreateRecipeRequestDto): Result<RecipeResponseDto>
}

class RecipesApiClientImpl(
    private val httpClient: HttpClient,
    private val authorizedRequestExecutor: AuthorizedRequestExecutor,
) : RecipesApiClient {

    override suspend fun getRecipes(
        authorId: String?,
        brewMethod: BrewMethodDto,
        difficulty: DifficultyDto?,
        limit: Int,
        page: Int
    ): Result<RecipesPageResponseDto> = authorizedRequestExecutor.execute { _ ->
        httpClient.get(ApiRoutes.Recipes) {
            contentType(ContentType.Application.Json)
            url {
                parameters.append("brew_method", getSerialName(brewMethod))
                difficulty?.let { parameters.append("difficulty", getSerialName(it)) }
                parameters.append("limit", limit.toString())
                parameters.append("page", page.toString())
            }
        }.body<RecipesPageResponseDto>()
    }

    override suspend fun addRecipe(recipe: CreateRecipeRequestDto): Result<RecipeResponseDto> =
        authorizedRequestExecutor.execute { _ ->
            httpClient.post(ApiRoutes.Recipes) {
                contentType(ContentType.Application.Json)
                setBody(recipe)
            }.body<RecipeResponseDto>()
        }

    override suspend fun updateRecipe(
        id: String, recipe: CreateRecipeRequestDto
    ): Result<RecipeResponseDto> = authorizedRequestExecutor.execute {
        httpClient.put(ApiRoutes.recipeById(id)) {
            contentType(ContentType.Application.Json)
            setBody(recipe)
        }.body<RecipeResponseDto>()
    }
}

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T : Enum<T>> getSerialName(value: T): String {
    return serializer<T>().descriptor.getElementName(value.ordinal)
}
