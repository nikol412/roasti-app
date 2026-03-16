package org.nikol.roasti.data.recipe

import org.nikol.roasti.data.recipe.mapper.toDomain
import org.nikol.roasti.data.recipe.mapper.toDto
import org.nikol.roasti.data.recipe.network.RecipesApiClient
import org.nikol.roasti.domain.recipe.BrewMethod
import org.nikol.roasti.domain.recipe.Difficulty
import org.nikol.roasti.domain.recipe.Recipe
import org.nikol.roasti.domain.recipe.RecipeRepository
import org.nikol.roasti.domain.recipe.RecipesPaginated

class NetworkRecipeRepository(
    private val apiClient: RecipesApiClient,
) : RecipeRepository {

    override suspend fun getRecipes(
        authorId: String?,
        brewMethod: BrewMethod?,
        difficulty: Difficulty?,
        limit: Int,
        page: Int
    ): Result<RecipesPaginated> {
        return apiClient.getRecipes(
            authorId = authorId,
            brewMethod = brewMethod?.toDto(),
            difficulty = difficulty?.toDto(),
            limit = limit,
            page = page
        ).mapCatching { it.toDomain() }
    }

    private suspend fun getRecipesOrNull(
        authorId: String? = null,
        brewMethod: BrewMethod? = null,
        difficulty: Difficulty? = null,
        limit: Int = 50,
        page: Int = 1,
    ): RecipesPaginated? = getRecipes(authorId, brewMethod, difficulty, limit, page).getOrNull()

    override suspend fun getById(id: String): Recipe? =
        getRecipesOrNull()?.items?.find { recipe -> recipe.id == id }

    override suspend fun addRecipe(recipe: Recipe): Result<Recipe> {
        return apiClient.addRecipe(recipe.toDto()).map { it.toDomain() }
    }
}
