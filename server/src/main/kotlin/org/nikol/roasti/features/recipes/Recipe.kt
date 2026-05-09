package org.nikol.roasti.features.recipes

import org.nikol.roasti.feature.recipe.domain.model.BrewMethod
import org.nikol.roasti.feature.recipe.domain.model.Difficulty
import org.nikol.roasti.feature.recipe.domain.model.RoastLevel
import org.nikol.roasti.features.users.UserId
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@JvmInline
@OptIn(ExperimentalUuidApi::class)
value class RecipeId(val value: Uuid)

data class RecipeAuthor(
    val id: UserId,
    val username: String,
    val avatarId: String?,
)

data class RecipeOriginInfo(
    val author: RecipeAuthor,
    val recipeId: RecipeId,
)

@OptIn(ExperimentalUuidApi::class)
data class Recipe(
    val id: RecipeId,
    val author: RecipeAuthor,
    val title: String,
    val description: String,
    val note: String?,
    val imageId: String?,
    val brewMethod: BrewMethod,
    val difficulty: Difficulty,
    val roastLevel: RoastLevel,
    val beans: String?,
    val public: Boolean,
    val steps: List<BrewStep>,
    val isLiked: Boolean,
    val likesCount: Int,
    val origin: RecipeOriginInfo?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class RecipesPage(
    val items: List<Recipe>,
    val currentPage: Int,
    val itemsCount: Int,
    val lastPage: Int,
    val nextPage: Int,
)
