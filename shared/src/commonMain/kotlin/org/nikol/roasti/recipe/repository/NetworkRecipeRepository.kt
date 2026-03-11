package org.nikol.roasti.recipe.repository

import org.nikol.roasti.recipe.model.Recipe
import org.nikol.roasti.recipe.model.RecipesPaginated
import org.nikol.roasti.recipe.network.RecipesApiClient
import org.nikol.roasti.recipe.network.dto.BrewMethodDto
import org.nikol.roasti.recipe.network.dto.DifficultyDto
import org.nikol.roasti.recipe.network.dto.toDomain

class NetworkRecipeRepository(
    private val apiClient: RecipesApiClient,
) : RecipeRepository {

    override suspend fun getRecipes(
        authorId: String?,
        brewMethod: BrewMethodDto?,
        difficulty: DifficultyDto?,
        limit: Int,
        page: Int
    ): Result<RecipesPaginated> {
        return apiClient.getRecipes(
            authorId = authorId,
            brewMethod = brewMethod,
            difficulty = difficulty,
            limit = limit,
            page = page
        ).mapCatching { it.toDomain() }
    }

    private suspend fun getRecipesOrNull(
        authorId: String? = null,
        brewMethod: BrewMethodDto? = null,
        difficulty: DifficultyDto? = null,
        limit: Int = 50,
        page: Int = 1,
    ): RecipesPaginated? = getRecipes(authorId, brewMethod, difficulty, limit, page).getOrNull()

    override suspend fun getById(id: String): Recipe? =
        getRecipesOrNull()?.items?.find { recipe -> recipe.id == id }
}
