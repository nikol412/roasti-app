package org.nikol.roasti.ui.uikit.post

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.nikol.roasti.ui.theme.RoastiTheme
import org.nikol.roasti.ui.theme.Spacing
import org.nikol.roasti.ui.uikit.AsyncImagePreviewProvider
import org.nikol.roasti.ui.uikit.AuthorRowWithTime
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Composable
fun PostCard(
    authorImageUrl: String?,
    authorName: String,
    postedAt: Instant?,
    title: String,
    body: String?,
    postImageUrl: String?,
    ratingState: PostRatingStateUi,
    commentsCount: Int,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
    onClick: () -> Unit = {},
    onRatingChange: (PostUserReaction) -> Unit = {},
    onCommentsClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
) {
    val ratingHandler = onRatingChange
    val commentsHandler = onCommentsClick
    val shareHandler = onShareClick

    var isBodyOverflowing by remember { mutableStateOf(false) }

    val rootModifier = if (isExpanded) {
        modifier.padding(horizontal = Spacing.lg, vertical = Spacing.lg)
    } else {
        modifier
            .clickable { onClick() }
            .padding(horizontal = Spacing.lg, vertical = Spacing.lg)
    }

    Column(
        rootModifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        AuthorRowWithTime(
            imageUrl = authorImageUrl,
            name = authorName,
            postedAt = postedAt,
        )

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
        )

        if (body != null) {
            Column {
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                    overflow = if (isExpanded) TextOverflow.Visible else TextOverflow.Ellipsis,
                    onTextLayout = { isBodyOverflowing = it.hasVisualOverflow },
                )
                if (!isExpanded && isBodyOverflowing) {
                    Text(
                        text = "Read More",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.clickable { onClick() },
                    )
                }
            }
        }

        if (postImageUrl != null) {
            PostImageCard(
                fullUrl = postImageUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .pointerInput(Unit) { detectTapGestures { } },
            )
        }

        PostOptionsRow(
            ratingValue = ratingState,
            commentsCount = commentsCount,
            listener = object : PostOptionsRowClickListener {
                override fun onChangeRating(newRatingIntent: PostUserReaction) =
                    ratingHandler(newRatingIntent)

                override fun onCommentsClick() = commentsHandler()
                override fun onShareClick() = shareHandler()
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PostCardPreview() {
    RoastiTheme {
        AsyncImagePreviewProvider {
            PostCard(
                authorImageUrl = "",
                authorName = "u/sarah_j",
                postedAt = Clock.System.now() - 0.5.hours - 2.minutes,
                title = "Dialing in the new Ethiopian Yirgacheffe this morning",
                body = "The floral notes are absolutely singing with a slightly coarser grind and a lower water temp (around 92°C). Definitely worth the extra few minutes of prep...",
                postImageUrl = "",
                ratingState = PostRatingStateUi(PostUserReaction.NONE, 124),
                commentsCount = 12,
            )
        }
    }
}
