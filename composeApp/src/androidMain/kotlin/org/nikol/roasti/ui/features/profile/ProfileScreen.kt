package org.nikol.roasti.ui.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.nikol.roasti.feature.auth.domain.model.User
import org.nikol.roasti.ui.theme.RoastiTheme
import org.nikol.roasti.ui.theme.Spacing
import org.nikol.roasti.ui.uikit.ErrorStub
import org.nikol.roasti.ui.uikit.LoadingStub

private const val HeaderTitle = "Profile"
private const val HeaderSubtitle = "Your session-aware corner of the app. Private data, synced identity, one logout away."
private const val UsernameLabel = "Username"
private const val UserIdLabel = "User ID"
private const val BioLabel = "Bio"
private const val MissingBioLabel = "No bio yet"
private const val LogoutLabel = "Log out"
private const val RefreshingLabel = "Syncing profile..."
private val HeaderShape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
private val CardShape = RoundedCornerShape(28.dp)
private val AvatarSize = 76.dp
private val ActionButtonHeight = 56.dp

@Composable
internal fun ProfileRoute(contentPadding: PaddingValues = PaddingValues()) {
    val viewModel: ProfileViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.syncProfile()
    }

    when (state) {
        ProfileUiState.Loading -> LoadingStub(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .consumeWindowInsets(contentPadding)
        )

        is ProfileUiState.Error -> ErrorStub(
            text = (state as ProfileUiState.Error).message,
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .consumeWindowInsets(contentPadding)
        )

        is ProfileUiState.Content -> ProfileScreenContent(
            state = state as ProfileUiState.Content,
            contentPadding = contentPadding,
            onLogoutClick = viewModel::logout,
        )
    }
}

@Composable
private fun ProfileScreenContent(
    state: ProfileUiState.Content,
    contentPadding: PaddingValues,
    onLogoutClick: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .consumeWindowInsets(contentPadding),
        verticalArrangement = Arrangement.spacedBy(Spacing.xl),
    ) {
        item {
            ProfileHeader(
                user = state.user,
                isRefreshing = state.isRefreshing,
            )
        }
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg),
                shape = CardShape,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.xxl),
                    verticalArrangement = Arrangement.spacedBy(Spacing.lg),
                ) {
                    ProfileFactRow(title = UsernameLabel, value = state.user.username)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f))
                    ProfileFactRow(title = UserIdLabel, value = state.user.id)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f))
                    ProfileFactRow(title = BioLabel, value = state.user.bio ?: MissingBioLabel)

                    Spacer(modifier = Modifier.height(Spacing.md))

                    Button(
                        onClick = onLogoutClick,
                        enabled = !state.isLoggingOut,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ActionButtonHeight),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        ),
                    ) {
                        if (state.isLoggingOut) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onError,
                                strokeWidth = 2.dp,
                            )
                            Spacer(modifier = Modifier.width(Spacing.md))
                        }
                        Text(
                            text = LogoutLabel,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    user: User,
    isRefreshing: Boolean,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = HeaderShape,
        color = MaterialTheme.colorScheme.primary,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.92f),
                        )
                    )
                )
                .padding(horizontal = Spacing.xxl, vertical = Spacing.xxxl),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            Box(
                modifier = Modifier
                    .size(AvatarSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = user.username.take(1).uppercase(),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
            Text(
                text = HeaderTitle,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Text(
                text = HeaderSubtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f),
            )
            if (isRefreshing) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.tertiary,
                        strokeWidth = 2.dp,
                    )
                    Text(
                        text = RefreshingLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.76f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileFactRow(
    title: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Preview
@Composable
private fun ProfileScreenPreview() {
    RoastiTheme {
        ProfileScreenContent(
            state = ProfileUiState.Content(
                user = User(
                    avatarId = null,
                    bio = "Balanced espresso, washed Ethiopians and weekend V60 flights.",
                    id = "user_42",
                    username = "origin_story",
                ),
                isRefreshing = false,
                isLoggingOut = false,
            ),
            contentPadding = PaddingValues(),
            onLogoutClick = {},
        )
    }
}
