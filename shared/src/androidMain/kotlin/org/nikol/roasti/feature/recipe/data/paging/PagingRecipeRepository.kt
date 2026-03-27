package org.nikol.roasti.feature.recipe.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import app.cash.sqldelight.paging3.QueryPagingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import org.nikol.roasti.FavoriteRecipe
import org.nikol.roasti.Recipe
import org.nikol.roasti.RoastiDatabaseCache
import org.nikol.roasti.feature.auth.domain.repository.AuthRepository
import org.nikol.roasti.feature.recipe.data.network.RecipesApiClient
import org.nikol.roasti.feature.likes.domain.LikesRepository

private const val RecipesPageSize = 20

@OptIn(ExperimentalPagingApi::class)
class PagingRecipeRepository(
    private val db: RoastiDatabaseCache,
    private val recipesApiClient: RecipesApiClient,
    private val authRepository: AuthRepository,
    private val favoritesRemoteMediator: FavoritesRemoteMediator,
    private val likesRepository: LikesRepository,
) {
    fun getRecipesPager(query: RecipesPagingQuery): Flow<PagingData<Recipe>> {
        return Pager(
            config = createPagingConfig(),
            remoteMediator = AllRecipesRemoteMediator(
                authRepository = authRepository,
                recipesApiClient = recipesApiClient,
                db = db,
                query = query,
            ),
            pagingSourceFactory = {
                QueryPagingSource(
                    countQuery = db.recipeQueries.countAllRecipes(),
                    transacter = db.recipeQueries,
                    context = chooseDispatcher(),
                    queryProvider = { limit, offset ->
                        db.recipeQueries.getAllRecipes(limit, offset)
                    }
                )
            }
        ).flow
    }

    fun getFavoritesPager(): Flow<PagingData<FavoriteRecipe>> {
        return Pager(
            config = createPagingConfig(),
            remoteMediator = favoritesRemoteMediator,
            pagingSourceFactory = {
                QueryPagingSource(
                    countQuery = db.favoriteRecipeQueries.countAllFavoriteRecipes(),
                    transacter = db.favoriteRecipeQueries,
                    context = chooseDispatcher(),
                    queryProvider = { limit, offset ->
                        db.favoriteRecipeQueries.getAllFavoriteRecipes(limit, offset)
                    }
                )
            }
        ).flow
    }

    suspend fun toggleLike(
        recipeId: String,
    ) {
        val recipe = db.recipeQueries.getRecipeById(recipeId).executeAsOneOrNull()
        val favoriteRecipe =
            db.favoriteRecipeQueries.getFavoriteRecipeById(recipeId).executeAsOneOrNull()

        val isCurrentlyLiked = recipe?.is_liked == 1L || favoriteRecipe != null

        db.transaction {
            if (recipe != null) {
                db.recipeQueries.toggleLike(recipeId)
            }

            if (isCurrentlyLiked) {
                db.favoriteRecipeQueries.deleteFavoriteRecipe(recipeId)
            } else {
                val r =
                    recipe  // recipe is in the main list
                if (r != null) {
                    db.favoriteRecipeQueries.insertFavoriteRecipe(
                        id = r.id,
                        title = r.title,
                        description = r.description,
                        image_id = r.image_id,
                        brew_method = r.brew_method,
                        difficulty = r.difficulty,
                        roast_level = r.roast_level,
                        beans = r.beans,
                        likes_count = r.likes_count + 1,
                        author_id = r.author_id,
                        author_name = r.author_name,
                        author_image_id = r.author_image_id,
                        liked_at = null,
                        created_at = r.created_at
                    )
                }
            }
        }

        likesRepository.toggleLikeOnRecipe(recipeId).onFailure {
            db.transaction {
                if (recipe != null) {
                    db.recipeQueries.toggleLike(recipeId) // toggle back
                }
                if (isCurrentlyLiked) {
                    // Was liked, we deleted it, now re-insert
                    if (favoriteRecipe != null) {
                        db.favoriteRecipeQueries.insertFavoriteRecipe(
                            id = favoriteRecipe.id,
                            title = favoriteRecipe.title,
                            description = favoriteRecipe.description,
                            image_id = favoriteRecipe.image_id,
                            brew_method = favoriteRecipe.brew_method,
                            difficulty = favoriteRecipe.difficulty,
                            roast_level = favoriteRecipe.roast_level,
                            beans = favoriteRecipe.beans,
                            likes_count = favoriteRecipe.likes_count,
                            author_id = favoriteRecipe.author_id,
                            author_name = favoriteRecipe.author_name,
                            author_image_id = favoriteRecipe.author_image_id,
                            liked_at = favoriteRecipe.liked_at,
                            created_at = favoriteRecipe.created_at
                        )
                    }
                } else {
                    // Was not liked, we inserted it, now delete
                    db.favoriteRecipeQueries.deleteFavoriteRecipe(recipeId)
                }
            }
        }
    }

    private fun createPagingConfig() =
        PagingConfig(pageSize = RecipesPageSize, prefetchDistance = 5, initialLoadSize = RecipesPageSize)

    private fun chooseDispatcher() = Dispatchers.IO
}
