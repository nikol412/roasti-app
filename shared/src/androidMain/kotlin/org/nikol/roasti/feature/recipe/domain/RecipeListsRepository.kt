package org.nikol.roasti.feature.recipe.domain

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.nikol.roasti.feature.recipe.domain.model.Recipe
import org.nikol.roasti.feature.recipe.domain.model.RecipesPagingQuery

interface RecipeListsRepository {
    fun observeHasCachedFeed(): Flow<Boolean>
    fun observeFeed(): Flow<PagingData<Recipe>>
    fun observeFavorites(): Flow<PagingData<Recipe>>
    fun observeSearch(query: RecipesPagingQuery): Flow<PagingData<Recipe>>
}
