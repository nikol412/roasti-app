package org.nikol.roasti.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.BookOpenText
import com.adamglin.phosphoricons.regular.Rows
import com.adamglin.phosphoricons.regular.User
import org.nikol.roasti.R
import org.nikol.roasti.navigation.Screen
import org.nikol.roasti.navigation.bottomNavScreens

private fun labelResFor(route: String): Int = when (route) {
    Screen.Feed.route -> R.string.bottom_nav_feed
    Screen.Recipes.route -> R.string.bottom_nav_recipes
    Screen.Profile.route -> R.string.bottom_nav_profile
    else -> R.string.app_name
}

@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
) {
    NavigationBar(
        modifier = modifier,
        windowInsets = NavigationBarDefaults.windowInsets,
    ) {
        bottomNavScreens.forEach { screen ->
            val isSelected = currentRoute == screen.route

            val color by animateColorAsState(
                if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onTertiaryContainer
            )
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (currentRoute != screen.route) {
                        onNavigate(screen.route)
                    }
                },
                icon = @Composable {
                    when (screen.route) {
                        Screen.Feed.route -> Icon(
                            imageVector = PhosphorIcons.Regular.Rows,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = color,
                        )

                        Screen.Recipes.route -> Icon(
                            imageVector = PhosphorIcons.Regular.BookOpenText,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = color,
                        )

                        Screen.Profile.route -> Icon(
                            imageVector = PhosphorIcons.Regular.User,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = color,
                        )
                    }
                },
                label = {
                    Text(
                        text = stringResource(labelResFor(screen.route)),
                        style = MaterialTheme.typography.labelSmall,
                        color = color,
                    )
                },
            )
        }
    }
}
