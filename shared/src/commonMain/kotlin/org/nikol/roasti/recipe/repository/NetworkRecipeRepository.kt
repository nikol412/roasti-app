package org.nikol.roasti.recipe.repository

import org.nikol.roasti.recipe.model.BrewMethod
import org.nikol.roasti.recipe.model.Difficulty
import org.nikol.roasti.recipe.model.Recipe
import org.nikol.roasti.recipe.model.RecipesPaginated
import org.nikol.roasti.recipe.network.RecipesApiClient
import org.nikol.roasti.recipe.network.dto.BrewMethodDto
import org.nikol.roasti.recipe.network.dto.DifficultyDto
import org.nikol.roasti.recipe.network.dto.toDomain

class NetworkRecipeRepository(
    private val apiClient: RecipesApiClient,
) : RecipeRepository {

    override suspend fun getRecipes(
        authorId: String?,
        brewMethod: BrewMethod?,
        difficulty: Difficulty?,
        limit: Int,
        page: Int
    ): Result<RecipesPaginated> {
        return apiClient.getRecipes(
            authorId = authorId,
            brewMethod = brewMethod?.toDto(),
            difficulty = difficulty?.toDto(),
            limit = limit,
            page = page
        ).mapCatching { it.toDomain() }
    }

    private suspend fun getRecipesOrNull(
        authorId: String? = null,
        brewMethod: BrewMethod? = null,
        difficulty: Difficulty? = null,
        limit: Int = 50,
        page: Int = 1,
    ): RecipesPaginated? = getRecipes(authorId, brewMethod, difficulty, limit, page).getOrNull()

    override suspend fun getById(id: String): Recipe? =
        getRecipesOrNull()?.items?.find { recipe -> recipe.id == id }
}

private fun BrewMethod.toDto(): BrewMethodDto = when (this) {
    BrewMethod.V60 -> BrewMethodDto.V60
    BrewMethod.FrenchPress -> BrewMethodDto.FRENCH_PRESS
    BrewMethod.Aeropress -> BrewMethodDto.AEROPRESS
    BrewMethod.Chemex -> BrewMethodDto.CHEMEX
    BrewMethod.ColdBrew -> BrewMethodDto.COLD_BREW
    BrewMethod.EspressoMachine -> BrewMethodDto.EXPRESSO_MACHINE
    BrewMethod.MokaPot -> BrewMethodDto.MOKA_POT
}

private fun Difficulty.toDto(): DifficultyDto = when (this) {
    Difficulty.Easy -> DifficultyDto.EASY
    Difficulty.Medium -> DifficultyDto.MEDIUM
    Difficulty.Hard -> DifficultyDto.HARD
}

