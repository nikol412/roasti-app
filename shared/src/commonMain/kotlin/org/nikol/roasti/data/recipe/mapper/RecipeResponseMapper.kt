package org.nikol.roasti.data.recipe.mapper

import org.nikol.roasti.data.recipe.remote.model.BrewMethodDto
import org.nikol.roasti.data.recipe.remote.model.DifficultyDto
import org.nikol.roasti.data.recipe.remote.model.RoastLevelDto
import org.nikol.roasti.data.recipe.remote.model.response.RecipeResponseDto
import org.nikol.roasti.data.recipe.remote.model.response.RecipeStepResponseDto
import org.nikol.roasti.data.recipe.remote.model.response.RecipesPageResponseDto
import org.nikol.roasti.domain.recipe.model.BrewMethod
import org.nikol.roasti.domain.recipe.model.BrewStep
import org.nikol.roasti.domain.recipe.model.Difficulty
import org.nikol.roasti.domain.recipe.model.Recipe
import org.nikol.roasti.domain.recipe.model.RecipesPage
import org.nikol.roasti.domain.recipe.model.RoastLevel

fun RecipesPageResponseDto.toDomain() = RecipesPage(
    items = items.map { it.toDomain() },
    page = page,
    limit = limit,
    totalCount = totalCount,
)

fun RecipeResponseDto.toDomain(): Recipe = Recipe(
    id = id,
    title = title,
    description = description,
    imageId = imageId,
    brewMethod = brewMethod.toDomain(),
    difficulty = difficulty.toDomain(),
    totalBrewTimeSeconds = steps.orEmpty().sumOf { step -> step.durationSeconds ?: 0 },
    roastLevel = roastLevel.toDomain(),
    beans = beans,
    steps = steps.orEmpty().map(RecipeStepResponseDto::toDomain),
)

fun RecipeStepResponseDto.toDomain(): BrewStep = BrewStep(
    order = order,
    title = title,
    description = description,
    durationSeconds = durationSeconds,
    imageId = imageId,
)

fun BrewMethodDto?.toDomain(): BrewMethod = when (this) {
    BrewMethodDto.V60 -> BrewMethod.V60
    BrewMethodDto.FRENCH_PRESS -> BrewMethod.FrenchPress
    BrewMethodDto.AEROPRESS -> BrewMethod.Aeropress
    BrewMethodDto.CHEMEX -> BrewMethod.Chemex
    BrewMethodDto.COLD_BREW -> BrewMethod.ColdBrew
    BrewMethodDto.EXPRESSO_MACHINE -> BrewMethod.EspressoMachine
    BrewMethodDto.MOKA_POT -> BrewMethod.MokaPot
    BrewMethodDto.NONE, null -> BrewMethod.NONE
}

fun DifficultyDto.toDomain(): Difficulty = when (this) {
    DifficultyDto.EASY -> Difficulty.Easy
    DifficultyDto.MEDIUM -> Difficulty.Medium
    DifficultyDto.HARD -> Difficulty.Hard
}

fun RoastLevelDto?.toDomain(): RoastLevel = when (this) {
    RoastLevelDto.LIGHT -> RoastLevel.Light
    RoastLevelDto.MEDIUM_LIGHT -> RoastLevel.MediumLight
    RoastLevelDto.MEDIUM -> RoastLevel.Medium
    RoastLevelDto.MEDIUM_DARK -> RoastLevel.MediumDark
    RoastLevelDto.DARK -> RoastLevel.Dark
    RoastLevelDto.NONE, null -> RoastLevel.NONE
}
