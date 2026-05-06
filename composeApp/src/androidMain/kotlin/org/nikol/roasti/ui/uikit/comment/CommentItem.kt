package org.nikol.roasti.ui.uikit.comment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import kotlinx.datetime.Instant
import org.nikol.roasti.R
import org.nikol.roasti.ui.theme.Spacing
import org.nikol.roasti.ui.uikit.AuthorRowWithTime

@Composable
fun CommentItem(
    isDeleted: Boolean,
    authorName: String?,
    authorAvatarUrl: String?,
    postedAt: Instant?,
    body: String,
    modifier: Modifier = Modifier,
) {
    val displayName = if (isDeleted || authorName == null) {
        stringResource(R.string.comments_deleted_placeholder)
    } else {
        authorName
    }
    val displayBody = if (isDeleted) stringResource(R.string.comments_deleted_body) else body
    val bodyColor = if (isDeleted) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = modifier.padding(vertical = Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        AuthorRowWithTime(
            imageUrl = if (isDeleted) null else authorAvatarUrl,
            name = displayName,
            postedAt = postedAt,
        )
        Text(
            text = displayBody,
            style = MaterialTheme.typography.bodyMedium,
            color = bodyColor,
            fontStyle = if (isDeleted) FontStyle.Italic else FontStyle.Normal,
        )
    }
}
