package org.nikol.roasti.feature.likes.domain

import org.nikol.roasti.feature.recipe.domain.model.Recipe

data class LikedRecipe(
    val likedAt: String,
    val recipe: Recipe,
)

data class LikedRecipesPage(
    val items: List<LikedRecipe>,
    val currentPage: Int,
    val itemsCount: Int,
    val lastPage: Int,
    val nextPage: Int,
)
