package org.nikol.roasti.recipe.repository

import org.nikol.roasti.recipe.model.BrewMethod
import org.nikol.roasti.recipe.model.BrewStep
import org.nikol.roasti.recipe.model.Difficulty
import org.nikol.roasti.recipe.model.Recipe
import org.nikol.roasti.recipe.model.RecipesPaginated
import org.nikol.roasti.recipe.model.RoastLevel
import org.nikol.roasti.recipe.network.RecipesApiClient
import org.nikol.roasti.recipe.network.dto.BrewMethodDto
import org.nikol.roasti.recipe.network.dto.BrewStepDto
import org.nikol.roasti.recipe.network.dto.DifficultyDto
import org.nikol.roasti.recipe.network.dto.RecipeDto
import org.nikol.roasti.recipe.network.dto.RoastLevelDto
import org.nikol.roasti.recipe.network.dto.toDomain
import org.nikol.roasti.recipe.network.toDomain

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

    override suspend fun addRecipe(recipe: Recipe): Result<Recipe> {
        return apiClient.addRecipe(recipe.toDto()).map { it.toDomain() }
    }
}

private fun Recipe.toDto() = RecipeDto(
    id = id,
    authorId = "",
    title = title,
    description = description,
    imageId = imageId,
    brewMethod = brewMethod?.toDto() ?: BrewMethodDto.NONE,
    difficulty = difficulty?.toDto() ?: DifficultyDto.NONE,
    roastLevel = roastLevel.toDto(),
    beans = beans,
    steps = steps.map { it.toDto() })

private fun BrewStep.toDto() = BrewStepDto(
    order = order,
    title = title,
    description = description,
    durationSeconds = durationSeconds,
    imageId = imageId,
)

fun RoastLevel?.toDto() = when (this) {
    RoastLevel.Dark -> RoastLevelDto.DARK
    RoastLevel.MediumDark -> RoastLevelDto.MEDIUM_DARK
    RoastLevel.Medium -> RoastLevelDto.MEDIUM
    RoastLevel.MediumLight -> RoastLevelDto.MEDIUM_LIGHT
    RoastLevel.Light -> RoastLevelDto.LIGHT
    else -> RoastLevelDto.NONE
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

