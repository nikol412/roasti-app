package org.nikol.roasti.domain.recipe

import org.nikol.roasti.domain.recipe.model.BrewMethod
import org.nikol.roasti.domain.recipe.model.Difficulty
import org.nikol.roasti.domain.recipe.model.Recipe
import org.nikol.roasti.domain.recipe.model.RecipeDraft
import org.nikol.roasti.domain.recipe.model.RecipesPage

interface RecipeRepository {
    suspend fun getRecipes(
        authorId: String? = null,
        brewMethod: BrewMethod? = null,
        difficulty: Difficulty? = null,
        limit: Int = 50,
        page: Int = 1
    ): Result<RecipesPage>

    suspend fun getById(id: String): Recipe?

    suspend fun addRecipe(recipe: RecipeDraft): Result<Recipe>

    suspend fun updateRecipe(id: String, recipe: RecipeDraft): Result<Recipe>
}
