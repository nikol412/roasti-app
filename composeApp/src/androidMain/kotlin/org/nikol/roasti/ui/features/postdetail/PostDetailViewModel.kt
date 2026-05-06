package org.nikol.roasti.ui.features.postdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nikol.roasti.feature.comment.data.paging.PagingCommentRepository
import org.nikol.roasti.feature.post.data.paging.PagingPostRepository
import org.nikol.roasti.ui.features.feed.mapper.toDomain
import org.nikol.roasti.ui.features.feed.mapper.toUiModel
import org.nikol.roasti.ui.features.feed.model.PostUiModel
import org.nikol.roasti.ui.features.postdetail.mapper.toUi
import org.nikol.roasti.ui.features.postdetail.model.CommentThreadUiModel
import org.nikol.roasti.ui.uikit.post.PostUserReaction
import org.nikol.roasti.utils.stateInWhileSubscribe

class PostDetailViewModel(
    private val postId: String,
    private val pagingPostRepository: PagingPostRepository,
    pagingCommentRepository: PagingCommentRepository,
) : ViewModel() {

    sealed interface HeaderState {
        data object Loading : HeaderState
        data object Error : HeaderState
        data class Content(val post: PostUiModel) : HeaderState
    }

    private val isHeaderRefreshFailed = MutableStateFlow(false)

    val headerState: StateFlow<HeaderState> = combine(
        pagingPostRepository.observePostById(postId),
        isHeaderRefreshFailed,
    ) { post, failed ->
        when {
            post != null -> HeaderState.Content(post.toUiModel())
            failed -> HeaderState.Error
            else -> HeaderState.Loading
        }
    }.stateInWhileSubscribe(HeaderState.Loading)

    val commentsPager: Flow<PagingData<CommentThreadUiModel>> =
        pagingCommentRepository.threadsPager(postId)
            .map { pagingData -> pagingData.map { it.toUi() } }
            .cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            pagingPostRepository.refreshPostById(postId)
                .onFailure { isHeaderRefreshFailed.update { true } }
        }
    }

    fun onRatingChange(intent: PostUserReaction) {
        viewModelScope.launch {
            pagingPostRepository.setVote(postId, intent.toDomain())
        }
    }
}
