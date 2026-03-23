package org.nikol.roasti.data.recipe

import org.nikol.roasti.data.recipe.mapper.toDomain
import org.nikol.roasti.data.recipe.mapper.toQueryDto
import org.nikol.roasti.data.recipe.mapper.toRequestDto
import org.nikol.roasti.data.recipe.network.RecipesApiClient
import org.nikol.roasti.domain.recipe.RecipeRepository
import org.nikol.roasti.domain.recipe.model.BrewMethod
import org.nikol.roasti.domain.recipe.model.Difficulty
import org.nikol.roasti.domain.recipe.model.Recipe
import org.nikol.roasti.domain.recipe.model.RecipeDraft
import org.nikol.roasti.domain.recipe.model.RecipesPage
import org.nikol.roasti.domain.recipe.model.RoastLevel

class NetworkRecipeRepository(
    private val apiClient: RecipesApiClient,
) : RecipeRepository {

    override suspend fun getRecipes(
        authorId: String?,
        brewMethod: BrewMethod?,
        difficulty: Difficulty?,
        roastLevel: RoastLevel?,
        limit: Int,
        page: Int
    ): Result<RecipesPage> {
        return apiClient.getRecipes(
            authorId = authorId,
            brewMethod = brewMethod.toRequestDto(),
            difficulty = difficulty.toQueryDto(),
            roastLevel = roastLevel.toQueryDto(),
            limit = limit,
            page = page
        ).mapCatching { it.toDomain() }
    }

    override suspend fun getById(id: String): Result<Recipe> =
        apiClient.getRecipe(id).map { it.toDomain() }

    override suspend fun addRecipe(recipe: RecipeDraft): Result<Recipe> {
        return apiClient.addRecipe(recipe.toRequestDto()).map { it.toDomain() }
    }

    override suspend fun updateRecipe(id: String, recipe: RecipeDraft): Result<Recipe> {
        return apiClient.updateRecipe(id, recipe.toRequestDto()).map { it.toDomain() }
    }

    override suspend fun removeRecipe(id: String): Result<Unit> = apiClient.removeRecipe(id)
}
