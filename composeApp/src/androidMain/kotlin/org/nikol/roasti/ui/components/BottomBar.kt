package org.nikol.roasti.ui.components

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.nikol.roasti.navigation.Screen
import org.nikol.roasti.navigation.bottomNavScreens

// Labels for each tab
private val labels = mapOf(
    Screen.Feed.route    to "Feed",
    Screen.Recipes.route to "Recipes",
    Screen.Profile.route to "Profile",
)

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
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = {
                    if (currentRoute != screen.route) {
                        onNavigate(screen.route)
                    }
                },
                // TODO: replace with icons when material-icons dependency is added
                icon = { Text(text = when (screen.route) {
                    Screen.Feed.route    -> "⊞"
                    Screen.Recipes.route -> "⊟"
                    Screen.Profile.route -> "⊙"
                    else -> ""
                }) },
                label = { Text(labels[screen.route] ?: "") },
            )
        }
    }
}
