package org.nikol.roasti.feature.recipe.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import kotlinx.coroutines.flow.firstOrNull
import org.nikol.roasti.FavoriteRecipe
import org.nikol.roasti.RoastiDatabaseCache
import org.nikol.roasti.feature.auth.domain.repository.AuthRepository
import org.nikol.roasti.feature.likes.data.LikesApiClient
import org.nikol.roasti.feature.recipe.data.mapper.toDomain

@OptIn(ExperimentalPagingApi::class)
class FavoritesRemoteMediator(
    private val likesApiClient: LikesApiClient,
    private val authRepository: AuthRepository,
    private val db: RoastiDatabaseCache,
) : RemoteMediator<Int, FavoriteRecipe>() {  // FavoriteRecipe = SQLDelight entity

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, FavoriteRecipe>
    ): MediatorResult {

        val page = when (loadType) {
            LoadType.REFRESH -> 1
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val remoteKey = db.recipeRemoteKeyQueries
                    .getRemoteKey("favorite_recipes")
                    .executeAsOneOrNull()
                remoteKey?.next_page?.toInt()
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
            }
        }

        return try {
            // Need userId for the likes endpoint
            val userId = authRepository.getUser().firstOrNull()?.id
                ?: return MediatorResult.Error(Exception("User not found"))

            val response = likesApiClient.getLikedRecipes(
                userId = userId,
                page = page,
                limit = state.config.pageSize
            ).getOrThrow()

            val items = response.items
            val pagination = response.pagination
            val endReached = pagination.currentPage >= pagination.lastPage

            db.transaction {
                if (loadType == LoadType.REFRESH) {
                    db.favoriteRecipeQueries.clearAllFavoriteRecipes()
                    db.recipeRemoteKeyQueries.clearRemoteKeys("favorite_recipes")
                }

                items.forEach { likedItem ->
                    val dto = likedItem.recipe
                    db.favoriteRecipeQueries.insertFavoriteRecipe(
                        id = dto.id,
                        title = dto.title,
                        description = dto.description,
                        image_id = dto.imageId,
                        brew_method = dto.brewMethod.toDomain(),
                        difficulty = dto.difficulty.toDomain(),
                        roast_level = dto.roastLevel.toDomain(),
                        beans = dto.beans,
                        likes_count = dto.likesCount.toLong(),
                        author_id = dto.author?.id,
                        author_name = dto.author?.username,
                        author_image_id = dto.author?.avatarId,
                        liked_at = likedItem.likedAt,
                        created_at = dto.createdAt
                    )
                }

                db.recipeRemoteKeyQueries.insertRemoteKey(
                    id = "favorite_recipes",
                    next_page = if (endReached) null else pagination.nextPage.toLong()
                )
            }

            MediatorResult.Success(endOfPaginationReached = endReached)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
