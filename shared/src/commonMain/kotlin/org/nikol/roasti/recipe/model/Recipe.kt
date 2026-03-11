package org.nikol.roasti.recipe.model

data class Recipe(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val brewMethod: BrewMethod?,
    val difficulty: Difficulty?,
    val totalBrewTimeSeconds: Int,
    val roastLevel: RoastLevel?,
    val beans: String?,
    val steps: List<BrewStep>,
)

data class RecipesPaginated(
    val items: List<Recipe>,
    val page: Int,
    val limit: Int,
    val totalCount: Int,
)
