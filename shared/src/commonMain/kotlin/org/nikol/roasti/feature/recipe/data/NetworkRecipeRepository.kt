package org.nikol.roasti.feature.recipe.data

import org.nikol.roasti.feature.recipe.data.mapper.toDomain
import org.nikol.roasti.feature.recipe.data.mapper.toQueryDto
import org.nikol.roasti.feature.recipe.data.mapper.toRequestDto
import org.nikol.roasti.feature.recipe.data.network.RecipesApiClient
import org.nikol.roasti.feature.recipe.domain.RecipeRepository
import org.nikol.roasti.feature.recipe.domain.model.BrewMethod
import org.nikol.roasti.feature.recipe.domain.model.Difficulty
import org.nikol.roasti.feature.recipe.domain.model.Recipe
import org.nikol.roasti.feature.recipe.domain.model.RecipeDraft
import org.nikol.roasti.feature.recipe.domain.model.RecipesPage
import org.nikol.roasti.feature.recipe.domain.model.RoastLevel

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
