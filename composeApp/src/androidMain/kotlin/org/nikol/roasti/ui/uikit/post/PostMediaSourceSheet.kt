package org.nikol.roasti.ui.uikit.post

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.nikol.roasti.R
import org.nikol.roasti.ui.uikit.RoastiBottomSheetShape

enum class PostMediaSource { CAMERA, GALLERY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostMediaSourceSheet(
    onPick: (PostMediaSource) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoastiBottomSheetShape,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Text(
                text = stringResource(R.string.post_media_source_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )

            SheetOptionRow(
                label = stringResource(R.string.post_media_source_camera),
                onClick = { onPick(PostMediaSource.CAMERA) },
            )
            SheetOptionRow(
                label = stringResource(R.string.post_media_source_gallery),
                onClick = { onPick(PostMediaSource.GALLERY) },
            )
        }
    }
}

@Composable
private fun SheetOptionRow(
    label: String,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    )
}
