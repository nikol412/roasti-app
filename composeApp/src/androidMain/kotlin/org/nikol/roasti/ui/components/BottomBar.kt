package org.nikol.roasti.ui.components

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import org.nikol.roasti.navigation.Screen
import org.nikol.roasti.navigation.bottomNavScreens

// Labels for each tab
private val labels = mapOf(
    Screen.Feed.route    to "Feed",
    Screen.Recipes.route to "Recipes",
    Screen.Profile.route to "Profile",
)

@Composable
fun BottomBar(navController: NavController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar {
        bottomNavScreens.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            // Pop up to Feed so back stack doesn't grow
                            popUpTo(Screen.Feed.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
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
