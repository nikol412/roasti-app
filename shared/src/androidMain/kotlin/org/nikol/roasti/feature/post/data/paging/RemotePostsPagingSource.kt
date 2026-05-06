package org.nikol.roasti.feature.post.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import org.nikol.roasti.feature.post.data.mapper.toDomain
import org.nikol.roasti.feature.post.data.network.PostsApiClient
import org.nikol.roasti.feature.post.domain.model.Post

private const val FirstPage = 1

/**
 * Network-only paging source for search results. Wired through [PagingPostRepository.getRemoteSearchPager]
 * but not yet exposed in UI — the search endpoint is not ready on the backend.
 */
class RemotePostsPagingSource(
    private val postsApiClient: PostsApiClient,
    private val query: PostsPagingQuery,
) : PagingSource<Int, Post>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Post> {
        val page = params.key ?: FirstPage

        return try {
            val response = postsApiClient.getPosts(
                page = page,
                limit = params.loadSize,
                query = query.query.takeIf { it.isNotBlank() },
            ).getOrThrow()

            LoadResult.Page(
                data = response.items.map { it.toDomain() },
                prevKey = if (page == FirstPage) null else page - 1,
                nextKey = response.pagination.nextPage.takeIf {
                    response.pagination.currentPage < response.pagination.lastPage
                },
            )
        } catch (error: Throwable) {
            LoadResult.Error(error)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Post>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val closestPage = state.closestPageToPosition(anchorPosition) ?: return null
        return closestPage.prevKey?.plus(1) ?: closestPage.nextKey?.minus(1)
    }
}
