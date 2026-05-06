package org.nikol.roasti.feature.post.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.paging3.QueryPagingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.nikol.roasti.Post as CachedPost
import org.nikol.roasti.RoastiDatabaseCache
import org.nikol.roasti.feature.post.data.mapper.toUserReaction
import org.nikol.roasti.feature.post.data.mapper.toWireString
import org.nikol.roasti.feature.post.data.network.PostsApiClient
import org.nikol.roasti.feature.post.data.remote.model.request.VoteRequestDto
import org.nikol.roasti.feature.post.data.mapper.toDto
import org.nikol.roasti.feature.post.domain.model.UserReaction

private const val PostsPageSize = 20
private const val PrefetchDistance = 5

@OptIn(ExperimentalPagingApi::class)
class PagingPostRepository(
    private val db: RoastiDatabaseCache,
    private val postsApiClient: PostsApiClient,
    private val allPostsRemoteMediator: AllPostsRemoteMediator,
) {
    fun observeHasCachedPosts(): Flow<Boolean> =
        db.postQueries.countAllPosts()
            .asFlow()
            .mapToOne(Dispatchers.IO)
            .map { count -> count > 0L }

    fun getOfflineFirstPostsPager(): Flow<PagingData<CachedPost>> = Pager(
        config = pagingConfig(),
        remoteMediator = allPostsRemoteMediator,
        pagingSourceFactory = {
            QueryPagingSource(
                countQuery = db.postQueries.countAllPosts(),
                transacter = db.postQueries,
                context = Dispatchers.IO,
                queryProvider = { limit, offset ->
                    db.postQueries.getAllPosts(limit, offset)
                },
            )
        },
    ).flow

    fun getRemoteSearchPager(
        query: PostsPagingQuery,
    ): Flow<PagingData<org.nikol.roasti.feature.post.domain.model.Post>> = Pager(
        config = pagingConfig(),
        pagingSourceFactory = {
            RemotePostsPagingSource(
                postsApiClient = postsApiClient,
                query = query,
            )
        },
    ).flow

    /**
     * Optimistic update: mutates the local Post row immediately, calls the API,
     * and reconciles to the server response on success or rolls back on failure.
     */
    suspend fun setVote(postId: String, target: UserReaction) {
        val current = db.postQueries.getPostById(postId).executeAsOneOrNull() ?: return
        val previousReaction = current.user_reaction.toUserReaction()
        if (previousReaction == target) return

        val previousRating = current.rating
        val previousReactionWire = current.user_reaction
        val optimisticRating = current.rating + previousReaction.deltaTo(target)
        val optimisticReactionWire = target.toWireString().takeUnless { target == UserReaction.NONE }

        db.transaction {
            db.postQueries.applyVote(
                rating = optimisticRating,
                user_reaction = optimisticReactionWire,
                id = postId,
            )
        }

        postsApiClient.vote(postId, VoteRequestDto(target.toDto())).fold(
            onSuccess = { dto ->
                db.transaction {
                    db.postQueries.applyVote(
                        rating = dto.rating.toLong(),
                        user_reaction = dto.userReaction,
                        id = postId,
                    )
                }
            },
            onFailure = {
                db.transaction {
                    db.postQueries.applyVote(
                        rating = previousRating,
                        user_reaction = previousReactionWire,
                        id = postId,
                    )
                }
            },
        )
    }

    private fun pagingConfig() = PagingConfig(
        pageSize = PostsPageSize,
        prefetchDistance = PrefetchDistance,
        initialLoadSize = PostsPageSize,
    )
}
