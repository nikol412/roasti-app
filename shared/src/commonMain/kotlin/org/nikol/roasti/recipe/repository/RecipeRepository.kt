package org.nikol.roasti.recipe.repository

import org.nikol.roasti.recipe.model.BrewMethod
import org.nikol.roasti.recipe.model.Difficulty
import org.nikol.roasti.recipe.model.Recipe
import org.nikol.roasti.recipe.model.RecipesPaginated

interface RecipeRepository {
    suspend fun getRecipes(
        authorId: String? = null,
        brewMethod: BrewMethod? = null,
        difficulty: Difficulty? = null,
        limit: Int = 50,
        page: Int = 1
    ): Result<RecipesPaginated>

    suspend fun getById(id: String): Recipe?
}
