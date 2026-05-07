package org.nikol.roasti.ui.screens

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.nikol.roasti.ui.features.postdetail.PostDetailScreen

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PostDetailRoute(
    postId: String,
    onClose: () -> Unit,
    onEditPost: (String) -> Unit = {},
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    PostDetailScreen(
        postId = postId,
        onClose = onClose,
        onEditPost = onEditPost,
        sharedTransitionScope = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope,
        modifier = Modifier.fillMaxSize(),
    )
}
