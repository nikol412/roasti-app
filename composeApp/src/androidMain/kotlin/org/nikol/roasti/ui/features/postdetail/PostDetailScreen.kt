package org.nikol.roasti.ui.features.postdetail

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.nikol.roasti.R
import org.nikol.roasti.ui.features.feed.model.PostUiModel
import org.nikol.roasti.ui.features.postdetail.model.CommentThreadUiModel
import org.nikol.roasti.ui.features.postdetail.model.CommentUiModel
import org.nikol.roasti.ui.theme.Spacing
import org.nikol.roasti.ui.uikit.ErrorStub
import org.nikol.roasti.ui.uikit.LoadingStub
import org.nikol.roasti.ui.uikit.comment.CommentItem
import org.nikol.roasti.ui.uikit.comment.CommentsEmptyState
import org.nikol.roasti.ui.uikit.post.DeletePostConfirmDialog
import org.nikol.roasti.ui.uikit.post.PostCard
import org.nikol.roasti.ui.uikit.post.PostOwnerActionsSheet
import org.nikol.roasti.ui.util.postCardSharedBoundsModifier

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun PostDetailScreen(
    postId: String,
    onClose: () -> Unit,
    onEditPost: (String) -> Unit,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    val viewModel: PostDetailViewModel = koinViewModel(parameters = { parametersOf(postId) })
    val headerState by viewModel.headerState.collectAsStateWithLifecycle()
    val comments = viewModel.commentsPager.collectAsLazyPagingItems()
    val listState = rememberLazyListState()

    val isOwn = (headerState as? PostDetailViewModel.HeaderState.Content)?.post?.isOwn == true
    var showOwnerSheet by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                PostDetailViewModel.PostDetailEvent.DeleteSuccess -> onClose()
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            PostDetailTopBar(
                onClose = onClose,
                showOwnerOptions = isOwn,
                onOwnerOptionsClick = { showOwnerSheet = true },
            )
        },
        bottomBar = {
            CommentInputBar()
        },
    ) { innerPadding ->
        when (val state = headerState) {
            PostDetailViewModel.HeaderState.Loading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding()),
            ) {
                LoadingStub(Modifier.align(Alignment.Center))
            }

            PostDetailViewModel.HeaderState.Error -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding()),
            ) {
                ErrorStub(
                    text = stringResource(R.string.post_detail_load_error),
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            is PostDetailViewModel.HeaderState.Content -> PostDetailContent(
                post = state.post,
                comments = comments,
                listState = listState,
                topInset = innerPadding.calculateTopPadding(),
                bottomInset = innerPadding.calculateBottomPadding(),
                onRatingChange = viewModel::onRatingChange,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
            )
        }
    }

    if (showOwnerSheet) {
        PostOwnerActionsSheet(
            onEdit = {
                showOwnerSheet = false
                onEditPost(postId)
            },
            onDelete = {
                showOwnerSheet = false
                showDeleteDialog = true
            },
            onDismiss = { showOwnerSheet = false },
        )
    }

    if (showDeleteDialog) {
        DeletePostConfirmDialog(
            onConfirm = {
                showDeleteDialog = false
                viewModel.onDeletePost()
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostDetailTopBar(
    onClose: () -> Unit,
    showOwnerOptions: Boolean,
    onOwnerOptionsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        title = {},
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.post_detail_close_label),
                )
            }
        },
        actions = {
            if (showOwnerOptions) {
                IconButton(onClick = onOwnerOptionsClick) {
                    Icon(
                        painter = painterResource(R.drawable.ic_three_dots),
                        contentDescription = stringResource(R.string.post_detail_more_options),
                    )
                }
            }
        },
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun PostDetailContent(
    post: PostUiModel,
    comments: LazyPagingItems<CommentThreadUiModel>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    topInset: androidx.compose.ui.unit.Dp,
    bottomInset: androidx.compose.ui.unit.Dp,
    onRatingChange: (org.nikol.roasti.ui.uikit.post.PostUserReaction) -> Unit,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    modifier: Modifier = Modifier,
) {
    val refreshState = comments.loadState.refresh
    val appendState = comments.loadState.append

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(top = topInset, bottom = bottomInset + Spacing.xxxl),
        modifier = modifier.fillMaxSize(),
    ) {
        item("post_header") {
            PostCard(
                authorImageUrl = post.authorImageUrl,
                authorName = post.authorName,
                postedAt = post.postedAt,
                title = post.title,
                body = post.body,
                postImageUrl = post.postImageUrl,
                ratingState = post.ratingState,
                commentsCount = post.commentsCount,
                isExpanded = true,
                onRatingChange = onRatingChange,
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
        }

        item("post_divider") {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.surfaceVariant,
                thickness = 8.dp,
            )
        }

        if (comments.itemCount > 0) {
            item("comments_section_title") {
                Text(
                    text = stringResource(R.string.comments_section_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(
                        horizontal = Spacing.lg,
                        vertical = Spacing.md,
                    ),
                )
            }
        }

        items(
            count = comments.itemCount,
            key = { index -> comments[index]?.root?.id ?: "thread_$index" },
        ) { index ->
            val thread = comments[index] ?: return@items
            CommentThreadBlock(
                thread = thread,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg),
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.lg),
            )
        }

        if (comments.itemCount == 0 && refreshState is LoadState.NotLoading) {
            item("comments_empty") {
                CommentsEmptyState(
                    title = stringResource(R.string.comments_empty_title),
                    subtitle = stringResource(R.string.comments_empty_subtitle),
                )
            }
        }

        if (comments.itemCount == 0 && refreshState is LoadState.Loading) {
            item("comments_loading") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.xxl),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }
        }

        if (refreshState is LoadState.Error) {
            item("comments_error") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.xxl),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.comments_load_error),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (appendState is LoadState.Loading) {
            item("comments_append_loading") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.lg),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentThreadBlock(
    thread: CommentThreadUiModel,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(vertical = Spacing.xs)) {
        CommentItem(
            isDeleted = thread.root.isDeleted,
            authorName = thread.root.authorName,
            authorAvatarUrl = thread.root.authorAvatarUrl,
            postedAt = thread.root.postedAt,
            body = thread.root.body,
        )
        thread.replies.forEach { reply ->
            ReplyRow(reply = reply)
        }
    }
}

@Composable
private fun ReplyRow(
    reply: CommentUiModel,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = Spacing.lg, top = Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
        CommentItem(
            isDeleted = reply.isDeleted,
            authorName = reply.authorName,
            authorAvatarUrl = reply.authorAvatarUrl,
            postedAt = reply.postedAt,
            body = reply.body,
        )
    }
}

@Composable
private fun CommentInputBar(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.large,
                    )
                    .clickable(enabled = false) {
                        // TODO: open comment composer when create flow lands
                    }
                    .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            ) {
                Text(
                    text = stringResource(R.string.comments_input_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
