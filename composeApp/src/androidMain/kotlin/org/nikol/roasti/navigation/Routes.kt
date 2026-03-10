package org.nikol.roasti.navigation

// 🔑 KMP: string routes work in commonMain without extra dependencies.
// Later upgrade path: @Serializable objects (type-safe nav) — needs kotlinx-serialization plugin.

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Feed : Screen("feed")
    object Recipes : Screen("recipes")
    object RecipeItem : Screen("recipe/{id}") {
        fun createRoute(id: String) = "recipe/$id"
    }

    object RecipeSteps : Screen("recipe/{id}/steps/{startStep}") {
        fun createRoute(id: String, startStep: Int = 0) = "recipe/$id/steps/$startStep"
    }

    object Profile : Screen("profile")
}

// Screens that show the bottom navigation bar
val bottomNavScreens = listOf(Screen.Feed, Screen.Recipes, Screen.Profile)
