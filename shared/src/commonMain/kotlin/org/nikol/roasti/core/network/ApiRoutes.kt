package org.nikol.roasti.core.network

object ApiRoutes {
    private const val ApiV1Path = "/api/v1"
    const val AuthPathPrefix = "$ApiV1Path/auth/"
    const val UsersPrefix = "$ApiV1Path/users/"
    const val Login = "${AuthPathPrefix}login"
    const val Register = "${AuthPathPrefix}register"
    const val Logout = "${AuthPathPrefix}logout"
    const val Refresh = "${AuthPathPrefix}refresh"
    const val Recipes = "$ApiV1Path/recipes"
    fun recipeById(id: String) = "$Recipes/$id"
    const val UploadsImages = "$ApiV1Path/uploads/images"
    const val ProfileMe = "$ApiV1Path/profiles/me"

    fun recipeLike(recipeId: String) = "$Recipes/$recipeId/like"
    fun userLikedRecipes(userId: String) = "$UsersPrefix$userId/likes"
}

object NetworkHeaders {
    const val BearerPrefix = "Bearer "
}
