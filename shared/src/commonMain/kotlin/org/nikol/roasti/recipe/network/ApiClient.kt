package org.nikol.roasti.recipe.network

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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import org.nikol.roasti.recipe.network.dto.BrewMethodDto
import org.nikol.roasti.recipe.network.dto.BrewStepDto
import org.nikol.roasti.recipe.network.dto.DifficultyDto
import org.nikol.roasti.recipe.network.dto.RecipeDto
import org.nikol.roasti.recipe.network.dto.RecipesResponseDto
import org.nikol.roasti.recipe.network.dto.RoastLevelDto

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

    suspend fun addRecipe(recipe: RecipeDto): Result<RecipeDto>
}

@Serializable
class UploadRecipeRequestBody(
    @SerialName("title") val title: String,
    @SerialName("beans") val beans: String? = null,
    @SerialName("brew_method") val brewMethod: BrewMethodDto? = null,
    @SerialName("description") val description: String,
    @SerialName("difficulty") val difficulty: DifficultyDto,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("roast_level") val roastLevel: RoastLevelDto? = null,
    @SerialName("steps") val steps: List<UploadRecipeStepRequestBody>,
)

@Serializable
class UploadRecipeStepRequestBody(
    @SerialName("description") val description: String,
    @SerialName("duration_seconds") val durationSeconds: Int? = null,
    @SerialName("order") val order: Int,
    @SerialName("title") val title: String,
)

fun RecipeDto.toRequest() = UploadRecipeRequestBody(title, beans, brewMethod, description, difficulty, imageUrl, roastLevel, steps.orEmpty().map { it.toRequest() })
fun BrewStepDto.toRequest() = UploadRecipeStepRequestBody(description, durationSeconds, order, title)

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

private fun HttpMessageBuilder.userIdHeader(id: String) = header(UserIdHeader, id)

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T : Enum<T>> getSerialName(value: T): String {
    return serializer<T>().descriptor.getElementName(value.ordinal)
}