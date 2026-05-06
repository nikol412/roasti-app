package org.nikol.roasti.feature.comment.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.paging3.QueryPagingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.nikol.roasti.RoastiDatabaseCache
import org.nikol.roasti.feature.comment.data.mapper.toDomain
import org.nikol.roasti.feature.comment.data.network.CommentsApiClient
import org.nikol.roasti.feature.comment.data.remote.model.request.CreateCommentRequestDto
import org.nikol.roasti.feature.comment.domain.model.Comment
import org.nikol.roasti.feature.comment.domain.model.CommentThread

private const val CommentsPageSize = 20
private const val PrefetchDistance = 5

@OptIn(ExperimentalPagingApi::class)
class PagingCommentRepository(
    private val db: RoastiDatabaseCache,
    private val commentsApiClient: CommentsApiClient,
) {

    fun observeHasCachedComments(postId: String): Flow<Boolean> =
        db.commentEntityQueries.countRootByPostId(postId)
            .asFlow()
            .mapToOne(Dispatchers.IO)
            .map { count -> count > 0L }

    fun threadsPager(postId: String): Flow<PagingData<CommentThread>> = Pager(
        config = pagingConfig(),
        remoteMediator = CommentsRemoteMediator(
            postId = postId,
            pageSize = CommentsPageSize,
            api = commentsApiClient,
            db = db,
        ),
        pagingSourceFactory = {
            QueryPagingSource(
                countQuery = db.commentEntityQueries.countRootByPostId(postId),
                transacter = db.commentEntityQueries,
                context = Dispatchers.IO,
                queryProvider = { limit, offset ->
                    db.commentEntityQueries.selectRootByPostIdPaged(postId, limit, offset)
                },
            )
        },
    ).flow.map { pagingData ->
        pagingData.map { row ->
            val replies = db.commentEntityQueries
                .selectRepliesByParentId(row.id)
                .executeAsList()
                .map { it.toDomain() }
            CommentThread(root = row.toDomain(), replies = replies)
        }
    }

    suspend fun createComment(
        postId: String,
        text: String,
        parentId: String? = null,
    ): Result<Comment> = commentsApiClient
        .createComment(postId, CreateCommentRequestDto(text = text, parentId = parentId))
        .map { it.toDomain() }

    private fun pagingConfig() = PagingConfig(
        pageSize = CommentsPageSize,
        prefetchDistance = PrefetchDistance,
        initialLoadSize = CommentsPageSize,
    )
}
