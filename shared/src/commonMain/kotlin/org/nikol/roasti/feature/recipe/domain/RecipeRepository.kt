package org.nikol.roasti.feature.recipe.domain

import org.nikol.roasti.feature.recipe.domain.model.BrewMethod
import org.nikol.roasti.feature.recipe.domain.model.Difficulty
import org.nikol.roasti.feature.recipe.domain.model.Recipe
import org.nikol.roasti.feature.recipe.domain.model.RecipeDraft
import org.nikol.roasti.feature.recipe.domain.model.RecipesPage
import org.nikol.roasti.feature.recipe.domain.model.RoastLevel

interface RecipeRepository {
    suspend fun getRecipes(
        authorId: String? = null,
        brewMethod: BrewMethod? = null,
        difficulty: Difficulty? = null,
        roastLevel: RoastLevel? = null,
        limit: Int = 50,
        page: Int = 1
    ): Result<RecipesPage>

    suspend fun getById(id: String): Result<Recipe>

    suspend fun addRecipe(recipe: RecipeDraft): Result<Recipe>

    suspend fun updateRecipe(id: String, recipe: RecipeDraft): Result<Recipe>

    suspend fun removeRecipe(id: String): Result<Unit>
}
