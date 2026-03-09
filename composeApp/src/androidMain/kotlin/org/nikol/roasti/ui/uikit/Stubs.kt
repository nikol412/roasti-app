package org.nikol.roasti.ui.uikit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.nikol.roasti.ui.theme.RoastiTypography

@Composable
internal fun LoadingStub(modifier: Modifier = Modifier, loaderSize: Dp = 64.dp) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.size(loaderSize),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

@Composable
internal fun ErrorStub(text: String, modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Text(text, style = RoastiTypography.titleLarge)
    }
}

@Preview
@Composable
private fun LoadingStubPreview() {
    LoadingStub()
}

@Preview
@Composable
private fun ErrorStubPreview() {
    ErrorStub("Something went wrong")
}