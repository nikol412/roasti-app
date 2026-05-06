package org.nikol.roasti.ui.features.feed

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import org.koin.compose.viewmodel.koinViewModel
import org.nikol.roasti.ui.components.bottomBarAware
import org.nikol.roasti.ui.features.feed.model.PostUiModel
import org.nikol.roasti.ui.theme.Spacing
import org.nikol.roasti.ui.uikit.ErrorStub
import org.nikol.roasti.ui.uikit.LoadingStub
import org.nikol.roasti.ui.uikit.SearchInput
import org.nikol.roasti.ui.uikit.post.PostCard
import org.nikol.roasti.ui.uikit.post.PostUserReaction
import org.nikol.roasti.ui.util.postCardSharedBoundsModifier

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun FeedScreen(
    contentPadding: PaddingValues,
    onPostClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    val viewModel: FeedViewModel = koinViewModel()

    val hasCachedPosts by viewModel.hasCachedPosts.collectAsStateWithLifecycle()
    val isManualRefresh by viewModel.isManualRefresh.collectAsStateWithLifecycle()
    val posts = viewModel.pagingPostsState.collectAsLazyPagingItems()
    val listState = rememberLazyListState()

    LaunchedEffect(isManualRefresh, posts.loadState.refresh) {
        if (isManualRefresh && posts.loadState.refresh !is LoadState.Loading) {
            viewModel.finishManualRefresh()
        }
    }

    val refreshState = posts.loadState.refresh
    val showFullScreenLoader = !hasCachedPosts && refreshState is LoadState.Loading
    val showFullScreenError = !hasCachedPosts && refreshState is LoadState.Error

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0),
        topBar = { FeedTopBar(scrollBehavior = scrollBehavior) },
    ) { innerPadding ->
        // LazyColumn fills the full body and pads its FIRST item below the TopAppBar via
        // contentPadding.top — so as the user scrolls, items pass under the bar and (once
        // the bar collapses via enterAlways) under the transparent system status bar.
        when {
            showFullScreenLoader -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding()),
            ) {
                LoadingStub(Modifier.align(Alignment.Center))
            }

            showFullScreenError -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding()),
            ) {
                ErrorStub(
                    text = "Failed to load feed",
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            else -> FeedContent(
                posts = posts,
                listState = listState,
                isManualRefresh = isManualRefresh,
                onRefresh = {
                    viewModel.startManualRefresh()
                    posts.refresh()
                },
                onRatingChange = viewModel::onRatingChange,
                onPostClick = onPostClick,
                topInset = innerPadding.calculateTopPadding(),
                bottomInset = contentPadding.calculateBottomPadding(),
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
) {
    val searchState = rememberTextFieldState()
    TopAppBar(
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        title = {
            // Left padding is supplied by TopAppBar's title slot (16dp by spec).
            // We add matching end padding to balance the right edge.
            SearchInput(
                state = searchState,
                placeholder = "Search blends, roasts...",
                enabled = false,
                onClick = {
                    // TODO: navigate to SearchScreen when search backend lands
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = Spacing.lg),
            )
        },
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun FeedContent(
    posts: LazyPagingItems<PostUiModel>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    isManualRefresh: Boolean,
    onRefresh: () -> Unit,
    onRatingChange: (PostUiModel, PostUserReaction) -> Unit,
    onPostClick: (String) -> Unit,
    topInset: androidx.compose.ui.unit.Dp,
    bottomInset: androidx.compose.ui.unit.Dp,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    modifier: Modifier = Modifier,
) {
    PullToRefreshBox(
        isRefreshing = isManualRefresh,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            contentPadding = PaddingValues(
                top = topInset,
                bottom = bottomInset + Spacing.xxxl,
            ),
            modifier = Modifier
                .fillMaxSize()
                .bottomBarAware(listState),
        ) {
            items(
                count = posts.itemCount,
                key = { index -> posts[index]?.id ?: "post_$index" },
            ) { index ->
                val post = posts[index] ?: return@items
                PostCard(
                    authorImageUrl = post.authorImageUrl,
                    authorName = post.authorName,
                    postedAt = post.postedAt,
                    title = post.title,
                    body = post.body,
                    postImageUrl = post.postImageUrl,
                    ratingState = post.ratingState,
                    commentsCount = post.commentsCount,
                    onRatingChange = { intent -> onRatingChange(post, intent) },
                    onClick = { onPostClick(post.id) },
                    onCommentsClick = { onPostClick(post.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            postCardSharedBoundsModifier(
                                postId = post.id,
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope,
                            )
                        ),
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(horizontal = Spacing.lg),
                )
            }

            if (posts.itemCount == 0 && posts.loadState.refresh is LoadState.NotLoading) {
                item("feed_empty") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.xxxl),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No posts yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            if (posts.loadState.append is LoadState.Loading) {
                item("feed_append_loading") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.lg),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
