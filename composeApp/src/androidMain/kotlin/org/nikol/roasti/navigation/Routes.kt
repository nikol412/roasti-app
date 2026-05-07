package org.nikol.roasti.navigation

// 🔑 KMP: string routes work in commonMain without extra dependencies.
// Later upgrade path: @Serializable objects (type-safe nav) — needs kotlinx-serialization plugin.

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Feed : Screen("feed")
    object Recipes : Screen("recipes")
    object RecipeItem : Screen("recipe/{id}") {
        fun createRoute(id: String) = "recipe/$id"
    }

    object RecipeSteps : Screen("recipe/{id}/steps/{startStep}") {
        fun createRoute(id: String, startStep: Int = 0) = "recipe/$id/steps/$startStep"
    }

    object EditRecipe : Screen("recipe/{id}/edit") {
        fun createRoute(id: String) = "recipe/$id/edit"
    }

    object CreateRecipe : Screen("recipe/create")

    object Profile : Screen("profile")

    object Settings : Screen("settings")

    object PostDetail : Screen("post/{id}") {
        const val ARG_ID = "id"
        fun createRoute(id: String) = "post/$id"
    }

    object PostCompose : Screen("post/compose?postId={postId}") {
        const val ARG_POST_ID = "postId"
        fun createRoute(postId: String? = null): String =
            if (postId == null) "post/compose" else "post/compose?postId=$postId"
    }
}

// Screens that show the bottom navigation bar
val bottomNavScreens = listOf(Screen.Feed, Screen.Recipes, Screen.Profile)
