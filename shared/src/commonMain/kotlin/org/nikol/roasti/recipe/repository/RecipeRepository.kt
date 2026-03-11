package org.nikol.roasti.recipe.repository

import org.nikol.roasti.recipe.model.Recipe
import org.nikol.roasti.recipe.model.RecipesPaginated
import org.nikol.roasti.recipe.network.dto.BrewMethodDto
import org.nikol.roasti.recipe.network.dto.DifficultyDto

interface RecipeRepository {
    suspend fun getRecipes(
        authorId: String? = null,
        brewMethod: BrewMethodDto? = null,
        difficulty: DifficultyDto? = null,
        limit: Int = 50,
        page: Int = 1
    ): Result<RecipesPaginated>
    suspend fun getById(id: String): Recipe?
}
