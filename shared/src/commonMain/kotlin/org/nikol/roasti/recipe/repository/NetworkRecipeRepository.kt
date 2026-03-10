package org.nikol.roasti.recipe.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import org.nikol.roasti.recipe.model.Recipe
import org.nikol.roasti.recipe.network.dto.RecipesResponseDto
import org.nikol.roasti.recipe.network.toDomain

private const val RecipesPath = "/api/v1/recipes"
private const val UserIdHeader = "X-User-Id"
private const val UserId = "test-user"

class NetworkRecipeRepository(
    private val httpClient: HttpClient,
    private val baseUrl: String,
) : RecipeRepository {

    override suspend fun getAll(): List<Recipe> = httpClient
        .get("$baseUrl$RecipesPath") {
            header(UserIdHeader, UserId)
        }
        .body<RecipesResponseDto>()
        .items
        .map { recipe -> recipe.toDomain() }

    override suspend fun getById(id: String): Recipe? = getAll().find { recipe -> recipe.id == id }

    override suspend fun search(query: String): List<Recipe> {
        val recipes = getAll()
        if (query.isBlank()) return recipes

        val lowerQuery = query.lowercase()
        return recipes.filter { recipe ->
            recipe.title.lowercase().contains(lowerQuery) ||
                recipe.description.lowercase().contains(lowerQuery) ||
                recipe.brewMethod.displayName.lowercase().contains(lowerQuery) ||
                recipe.beans?.lowercase()?.contains(lowerQuery) == true
        }
    }
}
