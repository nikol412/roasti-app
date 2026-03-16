package org.nikol.roasti.domain.recipe

interface RecipeRepository {
    suspend fun getRecipes(
        authorId: String? = null,
        brewMethod: BrewMethod? = null,
        difficulty: Difficulty? = null,
        limit: Int = 50,
        page: Int = 1
    ): Result<RecipesPaginated>

    suspend fun getById(id: String): Recipe?

    suspend fun addRecipe(recipe: Recipe): Result<Recipe>
}
