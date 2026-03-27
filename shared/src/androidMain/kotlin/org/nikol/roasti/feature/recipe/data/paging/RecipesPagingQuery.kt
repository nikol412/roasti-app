package org.nikol.roasti.feature.recipe.data.paging

import org.nikol.roasti.feature.recipe.domain.model.BrewMethod
import org.nikol.roasti.feature.recipe.domain.model.Difficulty
import org.nikol.roasti.feature.recipe.domain.model.RoastLevel

data class RecipesPagingQuery(
    val query: String = "",
    val brewMethod: BrewMethod? = null,
    val difficulty: Difficulty? = null,
    val roastLevel: RoastLevel? = null,
) {
    val isDefaultFeed: Boolean
        get() = query.isBlank() &&
                brewMethod == null &&
                difficulty == null &&
                roastLevel == null
}
