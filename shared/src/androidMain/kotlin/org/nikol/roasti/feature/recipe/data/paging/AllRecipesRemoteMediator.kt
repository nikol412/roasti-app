package org.nikol.roasti.feature.recipe.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import kotlinx.coroutines.flow.first
import org.nikol.roasti.Recipe
import org.nikol.roasti.RoastiDatabaseCache
import org.nikol.roasti.feature.auth.domain.repository.AuthRepository
import org.nikol.roasti.feature.recipe.data.network.RecipesApiClient
import org.nikol.roasti.feature.recipe.data.mapper.upsertRecipe

private const val AllRecipesRemoteKeyId = "all_recipes"

@OptIn(ExperimentalPagingApi::class)
class AllRecipesRemoteMediator(
    private val authRepository: AuthRepository,
    private val recipesApiClient: RecipesApiClient,
    private val db: RoastiDatabaseCache,
) : RemoteMediator<Int, Recipe>() {

    private var userId: String? = null

    override suspend fun initialize(): RemoteMediator.InitializeAction {
        return RemoteMediator.InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, Recipe>): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                1
            }

            LoadType.PREPEND -> {
                return MediatorResult.Success(endOfPaginationReached = true)
            }

            LoadType.APPEND -> {
                val remoteKey = db.recipeRemoteKeyQueries.getRemoteKey(AllRecipesRemoteKeyId).executeAsOneOrNull()
                remoteKey?.next_page?.toInt() ?: return MediatorResult.Success(endOfPaginationReached = true)
            }
        }

        return try {
            val response = recipesApiClient.getRecipes(
                authorId = assignCurrentUser(),
                page = page,
                limit = state.config.pageSize,
            ).getOrThrow()

            val recipes = response.items
            val pagination = response.pagination
            val endReached = pagination.currentPage >= pagination.lastPage

            db.transaction {
                if (loadType == LoadType.REFRESH) {
                    db.recipeQueries.clearAllRecipes()
                    db.recipeRemoteKeyQueries.clearRemoteKeys(AllRecipesRemoteKeyId)
                }

                recipes.forEach { dto ->
                    db.upsertRecipe(dto)
                }

                db.recipeRemoteKeyQueries.insertRemoteKey(
                    AllRecipesRemoteKeyId,
                    if (endReached) null else pagination.nextPage.toLong()
                )
            }

            MediatorResult.Success(endOfPaginationReached = endReached)
        } catch (th: Throwable) {
            MediatorResult.Error(th)
        }
    }

    private suspend fun assignCurrentUser(): String? {
        if(userId == null) {
            userId = authRepository.getUser().first()?.id
        }
        return userId
    }
}
