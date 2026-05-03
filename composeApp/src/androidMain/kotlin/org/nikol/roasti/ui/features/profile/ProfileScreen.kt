package org.nikol.roasti.ui.features.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.nikol.roasti.ui.features.profile.widgets.ProfileFavoriteRecipesRow
import org.nikol.roasti.ui.features.profile.widgets.ProfileHeaderRow
import org.nikol.roasti.ui.features.profile.widgets.StatisticsRow

@Composable
internal fun ProfileRoute(
    onNavigateToSettings: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val viewModel: ProfileViewModel = koinViewModel()
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    val listener = remember(viewModel, onNavigateToSettings) {
        object : ProfileRowListener {
            override fun onImagePicked(fileName: String, bytes: ByteArray) =
                viewModel.onImagePicked(fileName, bytes)

            override fun onEditClick() = viewModel.onEditClick()
            override fun onSettingsClick() = onNavigateToSettings()
            override fun onLogoutClick() = viewModel.onLogoutClick()
        }
    }

    ProfileScreen(uiState, listener)
}

@Composable
private fun ProfileScreen(
    uiState: ProfileState,
    listener: ProfileRowListener,
) {
    Scaffold() { innerPaddings ->
        Column(Modifier.padding(innerPaddings)) {
            ProfileHeaderRow(
                userUiModel = uiState.user,
                listener = listener,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            StatisticsRow(
                item = uiState.statistics,
                modifier = Modifier
                    .padding(top = 20.dp)
                    .padding(16.dp)
            )
            ProfileFavoriteRecipesRow(
                item = uiState.favoritesState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalPaddings = 16.dp
            )

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileRootPreview() {
    ProfileScreen(uiState = ProfileState(), listener = object : ProfileRowListener {
        override fun onImagePicked(fileName: String, bytes: ByteArray) {}
        override fun onEditClick() {}
        override fun onSettingsClick() {}
        override fun onLogoutClick() {}
    })
}

