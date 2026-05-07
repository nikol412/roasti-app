package org.nikol.roasti.core.database

import org.nikol.roasti.RoastiDatabaseCache

/**
 * Wipes every table that holds user-scoped data. Call on logout or when the current
 * session is detected to be stale, so the next signed-in user does not see leftovers
 * from the previous one.
 */
fun RoastiDatabaseCache.clearAllUserScopedData() {
    transaction {
        postQueries.clearAllPosts()
        postRemoteKeyQueries.clearAllRemoteKeys()
        commentEntityQueries.clearAllComments()
        commentRemoteKeyQueries.clearAllRemoteKeys()
        recipeQueries.clearAllRecipes()
        recipeStepQueries.clearAllRecipeSteps()
        recipeRemoteKeyQueries.clearAllRemoteKeys()
        favoriteRecipeQueries.clearAllFavoriteRecipes()
    }
}
