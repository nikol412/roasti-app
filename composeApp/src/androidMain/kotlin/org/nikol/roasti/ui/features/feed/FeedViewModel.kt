package org.nikol.roasti.ui.features.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.nikol.roasti.feature.post.data.mapper.toDomain
import org.nikol.roasti.feature.post.data.paging.PagingPostRepository
import org.nikol.roasti.feature.post.data.paging.PostsPagingQuery
import org.nikol.roasti.ui.features.feed.mapper.toDomain
import org.nikol.roasti.ui.features.feed.mapper.toUiModel
import org.nikol.roasti.ui.features.feed.model.PostUiModel
import org.nikol.roasti.ui.uikit.post.PostUserReaction

private const val SearchQueryDebounceMillis = 300L

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class FeedViewModel(
    private val pagingPostRepository: PagingPostRepository,
) : ViewModel() {

    val hasCachedPosts: StateFlow<Boolean> =
        pagingPostRepository.observeHasCachedPosts()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = false,
            )

    private val searchQueryMutable = MutableStateFlow("")
    val searchQuery: StateFlow<String> = searchQueryMutable.asStateFlow()

    private val manualRefreshMutable = MutableStateFlow(false)
    val isManualRefresh: StateFlow<Boolean> = manualRefreshMutable.asStateFlow()

    private val postsQuery: Flow<PostsPagingQuery> =
        searchQueryMutable
            .debounce { query -> if (query.isBlank()) 0L else SearchQueryDebounceMillis }
            .map(String::trim)
            .distinctUntilChanged()
            .map(::PostsPagingQuery)

    val pagingPostsState: Flow<PagingData<PostUiModel>> =
        postsQuery
            .flatMapLatest { query ->
                if (query.isDefaultFeed) {
                    pagingPostRepository.getOfflineFirstPostsPager()
                        .map { pagingData -> pagingData.map { it.toDomain().toUiModel() } }
                } else {
                    pagingPostRepository.getRemoteSearchPager(query)
                        .map { pagingData -> pagingData.map { it.toUiModel() } }
                }
            }
            .cachedIn(viewModelScope)

    /**
     * Search plumbing is wired but not yet exposed in the UI. When the backend search endpoint
     * lands, a search bar can call this method directly with no other architectural changes.
     */
    fun search(query: String) {
        searchQueryMutable.value = query
    }

    fun onRatingChange(post: PostUiModel, intent: PostUserReaction) {
        viewModelScope.launch {
            pagingPostRepository.setVote(post.id, intent.toDomain())
        }
    }

    fun startManualRefresh() {
        manualRefreshMutable.value = true
    }

    fun finishManualRefresh() {
        manualRefreshMutable.value = false
    }
}
