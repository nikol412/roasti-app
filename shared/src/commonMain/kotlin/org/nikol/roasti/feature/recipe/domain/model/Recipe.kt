package org.nikol.roasti.feature.recipe.domain.model

data class Recipe(
    val id: String,
    val title: String,
    val description: String,
    val imageId: String?,
    val brewMethod: BrewMethod,
    val difficulty: Difficulty,
    val roastLevel: RoastLevel,
    val beans: String?,
    val steps: List<BrewStep>,
    val author: Author?,
    val isLiked: Boolean,
    val likesCount: Int,
    val origin: RecipeOrigin?,
    val isPublic: Boolean,
    val createdAt: String?,
    val updatedAt: String?,
)

data class RecipesPage(
    val items: List<Recipe>,
    val currentPage: Int,
    val itemsCount: Int,
    val lastPage: Int,
    val nextPage: Int,
)
