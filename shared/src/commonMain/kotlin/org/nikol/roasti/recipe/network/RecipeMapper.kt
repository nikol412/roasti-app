package org.nikol.roasti.recipe.network

import org.nikol.roasti.recipe.model.BrewMethod
import org.nikol.roasti.recipe.model.BrewStep
import org.nikol.roasti.recipe.model.Difficulty
import org.nikol.roasti.recipe.model.Recipe
import org.nikol.roasti.recipe.model.RoastLevel
import org.nikol.roasti.recipe.network.dto.BrewMethodDto
import org.nikol.roasti.recipe.network.dto.BrewStepDto
import org.nikol.roasti.recipe.network.dto.DifficultyDto
import org.nikol.roasti.recipe.network.dto.RecipeDto
import org.nikol.roasti.recipe.network.dto.RoastLevelDto

fun RecipeDto.toDomain(): Recipe = Recipe(
    id = id,
    title = title,
    description = description,
    imageUrl = imageUrl,
    brewMethod = brewMethod.toDomain(),
    difficulty = difficulty.toDomain(),
    totalBrewTimeSeconds = steps.sumOf { step -> step.durationSeconds ?: 0 },
    roastLevel = roastLevel?.toDomain(),
    beans = beans,
    steps = steps.map(BrewStepDto::toDomain),
)

private fun BrewStepDto.toDomain(): BrewStep = BrewStep(
    order = order,
    title = title,
    description = description,
    durationSeconds = durationSeconds,
)

private fun BrewMethodDto.toDomain(): BrewMethod? = when (this) {
    BrewMethodDto.V60 -> BrewMethod.V60
    BrewMethodDto.FRENCH_PRESS -> BrewMethod.FrenchPress
    BrewMethodDto.AEROPRESS -> BrewMethod.Aeropress
    BrewMethodDto.CHEMEX -> BrewMethod.Chemex
    BrewMethodDto.COLD_BREW -> BrewMethod.ColdBrew
    BrewMethodDto.EXPRESSO_MACHINE -> BrewMethod.EspressoMachine
    BrewMethodDto.MOKA_POT -> BrewMethod.MokaPot
    BrewMethodDto.NONE -> null
}

private fun DifficultyDto.toDomain(): Difficulty? = when (this) {
    DifficultyDto.EASY -> Difficulty.Easy
    DifficultyDto.MEDIUM -> Difficulty.Medium
    DifficultyDto.HARD -> Difficulty.Hard
    DifficultyDto.NONE -> null
}

private fun RoastLevelDto.toDomain(): RoastLevel? = when (this) {
    RoastLevelDto.LIGHT -> RoastLevel.Light
    RoastLevelDto.MEDIUM_LIGHT -> RoastLevel.MediumLight
    RoastLevelDto.MEDIUM -> RoastLevel.Medium
    RoastLevelDto.MEDIUM_DARK -> RoastLevel.MediumDark
    RoastLevelDto.DARK -> RoastLevel.Dark
    RoastLevelDto.NONE -> null
}
