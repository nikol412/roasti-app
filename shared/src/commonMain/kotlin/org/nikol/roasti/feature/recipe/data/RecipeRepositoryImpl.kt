package org.nikol.roasti.feature.recipe.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.nikol.roasti.RoastiDatabaseCache
import org.nikol.roasti.feature.recipe.data.mapper.upsertRecipe
import org.nikol.roasti.feature.recipe.data.mapper.toDomain
import org.nikol.roasti.feature.recipe.data.mapper.toQueryDto
import org.nikol.roasti.feature.recipe.data.mapper.toRequestDto
import org.nikol.roasti.feature.recipe.data.network.RecipesApiClient
import org.nikol.roasti.feature.recipe.domain.RecipeRepository
import org.nikol.roasti.feature.recipe.domain.model.BrewMethod
import org.nikol.roasti.feature.recipe.domain.model.Difficulty
import org.nikol.roasti.feature.recipe.domain.model.Recipe
import org.nikol.roasti.feature.recipe.domain.model.RecipeDraft
import org.nikol.roasti.core.domain.Page
import org.nikol.roasti.feature.recipe.domain.model.RoastLevel

class RecipeRepositoryImpl(
    private val apiClient: RecipesApiClient,
    private val db: RoastiDatabaseCache,
) : RecipeRepository {

    override suspend fun getRecipes(
        authorId: String?,
        query: String?,
        brewMethod: BrewMethod?,
        difficulty: Difficulty?,
        roastLevel: RoastLevel?,
        limit: Int,
        page: Int
    ): Result<Page<Recipe>> {
        return apiClient.getRecipes(
            authorId = authorId,
            query = query?.takeIf { it.isNotBlank() },
            brewMethod = brewMethod?.toRequestDto(),
            difficulty = difficulty.toQueryDto(),
            roastLevel = roastLevel.toQueryDto(),
            limit = limit,
            page = page
        ).mapCatching { it.toDomain() }
    }

    override suspend fun getById(id: String): Result<Recipe> =
        apiClient.getRecipe(id)
            .mapCatching { recipe ->
                db.transaction {
                    db.upsertRecipe(recipe)
                }
                recipe.toDomain()
            }
            .recoverCatching {
                cachedRecipeById(id) ?: throw it
            }

    override fun getByIdFlow(id: String): Flow<Recipe> = flow {
        val cachedRecipe = cachedRecipeById(id)
        if (cachedRecipe != null) {
            emit(cachedRecipe)
        }

        val remoteRecipe = apiClient.getRecipe(id).getOrElse { error ->
            if (cachedRecipe == null) throw error
            return@flow
        }

        db.transaction {
            db.upsertRecipe(remoteRecipe)
        }

        emit(remoteRecipe.toDomain())
    }

    override suspend fun addRecipe(recipe: RecipeDraft): Result<Recipe> {
        return apiClient.addRecipe(recipe.toRequestDto()).mapCatching {
            db.transaction {
                db.upsertRecipe(it)
            }
            it.toDomain()
        }
    }

    override suspend fun updateRecipe(id: String, recipe: RecipeDraft): Result<Recipe> {
        return apiClient.updateRecipe(id, recipe.toRequestDto()).mapCatching {
            db.transaction {
                db.upsertRecipe(it)
            }
            it.toDomain()
        }
    }

    override suspend fun removeRecipe(id: String): Result<Unit> =
        apiClient.removeRecipe(id).onSuccess {
            db.transaction {
                db.recipeQueries.deleteRecipe(id)
                db.recipeStepQueries.deleteRecipeStepsByRecipeId(id)
                db.favoriteRecipeQueries.deleteFavoriteRecipe(id)
            }
        }

    private fun cachedRecipeById(id: String): Recipe? {
        val cachedRecipe = db.recipeQueries.getRecipeById(id).executeAsOneOrNull()
        val cachedFavorite = db.favoriteRecipeQueries.getFavoriteRecipeById(id).executeAsOneOrNull()
        val steps = db.recipeStepQueries.getRecipeStepsByRecipeId(id).executeAsList()

        return when {
            cachedRecipe != null -> cachedRecipe.toDomain(steps)
            cachedFavorite != null -> cachedFavorite.toDomain(steps)
            else -> null
        }
    }
}
