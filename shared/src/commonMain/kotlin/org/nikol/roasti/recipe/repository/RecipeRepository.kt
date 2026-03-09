package org.nikol.roasti.recipe.repository

import org.nikol.roasti.recipe.model.Recipe

interface RecipeRepository {
    suspend fun getAll(): List<Recipe>
    suspend fun getById(id: String): Recipe?
    suspend fun search(query: String): List<Recipe>
}
