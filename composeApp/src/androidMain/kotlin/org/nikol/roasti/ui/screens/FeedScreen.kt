package org.nikol.roasti.ui.screens

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.nikol.roasti.ui.features.feed.FeedScreen

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun FeedRoute(
    contentPadding: PaddingValues = PaddingValues(),
    onPostClick: (String) -> Unit = {},
    onCreatePost: () -> Unit = {},
    onEditPost: (String) -> Unit = {},
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    FeedScreen(
        contentPadding = contentPadding,
        onPostClick = onPostClick,
        onCreatePost = onCreatePost,
        onEditPost = onEditPost,
        sharedTransitionScope = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope,
        modifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(contentPadding),
    )
}
