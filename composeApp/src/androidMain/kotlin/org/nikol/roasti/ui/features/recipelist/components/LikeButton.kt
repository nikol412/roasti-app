package org.nikol.roasti.ui.features.recipelist.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.nikol.roasti.R

@Composable
internal fun LikeButton(
    isLiked: Boolean,
    likesCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        if (likesCount > 0) {
            Text(
                text = likesCount.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = if (isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                painter = painterResource(
                    if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart_outlined
                ),
                contentDescription = null,
                tint = if (isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}