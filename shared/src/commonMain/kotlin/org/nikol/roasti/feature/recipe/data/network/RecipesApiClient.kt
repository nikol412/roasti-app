package org.nikol.roasti.feature.recipe.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.prepareDelete
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.serializer
import org.nikol.roasti.core.network.ApiRoutes
import org.nikol.roasti.core.network.AuthorizedRequestExecutor
import org.nikol.roasti.feature.recipe.data.remote.model.BrewMethodDto
import org.nikol.roasti.feature.recipe.data.remote.model.DifficultyDto
import org.nikol.roasti.feature.recipe.data.remote.model.RoastLevelDto
import org.nikol.roasti.feature.recipe.data.remote.model.request.CreateRecipeRequestDto
import org.nikol.roasti.feature.recipe.data.remote.model.response.RecipeResponseDto
import org.nikol.roasti.feature.recipe.data.remote.model.response.RecipesPageResponseDto

interface RecipesApiClient {
    suspend fun getRecipes(
        authorId: String? = null,
        brewMethod: BrewMethodDto = BrewMethodDto.NONE,
        difficulty: DifficultyDto? = null,
        roastLevel: RoastLevelDto? = null,
        limit: Int = 50,
        page: Int = 1
    ): Result<RecipesPageResponseDto>

    suspend fun getRecipe(id: String): Result<RecipeResponseDto>
    suspend fun addRecipe(recipe: CreateRecipeRequestDto): Result<RecipeResponseDto>

    suspend fun updateRecipe(id: String, recipe: CreateRecipeRequestDto): Result<RecipeResponseDto>

    suspend fun removeRecipe(id: String): Result<Unit>
}

class RecipesApiClientImpl(
    private val httpClient: HttpClient,
    private val authorizedRequestExecutor: AuthorizedRequestExecutor,
) : RecipesApiClient {

    override suspend fun getRecipes(
        authorId: String?,
        brewMethod: BrewMethodDto,
        difficulty: DifficultyDto?,
        roastLevel: RoastLevelDto?,
        limit: Int,
        page: Int
    ): Result<RecipesPageResponseDto> = authorizedRequestExecutor.execute { _ ->
        httpClient.get(ApiRoutes.Recipes) {
            url {
                parameters.append("brew_method", getSerialName(brewMethod))
                difficulty?.let { parameters.append("difficulty", getSerialName(it)) }
                roastLevel?.let { parameters.append("roast_level", getSerialName(it)) }
                parameters.append("limit", limit.toString())
                parameters.append("page", page.toString())
            }
        }.body<RecipesPageResponseDto>()
    }

    override suspend fun getRecipe(id: String): Result<RecipeResponseDto> =
        authorizedRequestExecutor.execute {
            httpClient.get(ApiRoutes.recipeById(id)).body<RecipeResponseDto>()
        }

    override suspend fun addRecipe(recipe: CreateRecipeRequestDto): Result<RecipeResponseDto> =
        authorizedRequestExecutor.execute { _ ->
            httpClient.post(ApiRoutes.Recipes) {
                setBody(recipe)
            }.body<RecipeResponseDto>()
        }

    override suspend fun updateRecipe(
        id: String, recipe: CreateRecipeRequestDto
    ): Result<RecipeResponseDto> = authorizedRequestExecutor.execute {
        httpClient.put(ApiRoutes.recipeById(id)) {
            setBody(recipe)
        }.body<RecipeResponseDto>()
    }

    override suspend fun removeRecipe(id: String): Result<Unit> =
        authorizedRequestExecutor.execute {
            val status = httpClient.delete(ApiRoutes.recipeById(id)).status

            return@execute if (status.isSuccess()) Unit else throw Throwable("error while deleting recipe")
        }
}

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T : Enum<T>> getSerialName(value: T): String {
    return serializer<T>().descriptor.getElementName(value.ordinal)
}
