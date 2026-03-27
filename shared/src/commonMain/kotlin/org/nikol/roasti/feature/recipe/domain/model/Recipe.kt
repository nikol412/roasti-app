package org.nikol.roasti.feature.recipe.domain.model

data class Recipe(
    val id: String,
    val title: String,
    val description: String,
    val note: String? = null,
    val imageId: String?,
    val brewMethod: BrewMethod,
    val difficulty: Difficulty,
    val roastLevel: RoastLevel,
    val beans: String?,
    val steps: List<BrewStep>,
    // Current author of the recipe.
    val author: Author?,
    val isLiked: Boolean,
    val likesCount: Int,
    // Contains the author of the original recipe if this recipe was cloned.
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
