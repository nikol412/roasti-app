package org.nikol.roasti.data.recipe.mapper

import org.nikol.roasti.data.recipe.dto.BrewMethodDto
import org.nikol.roasti.data.recipe.dto.BrewStepDto
import org.nikol.roasti.data.recipe.dto.DifficultyDto
import org.nikol.roasti.data.recipe.dto.RecipeDto
import org.nikol.roasti.data.recipe.dto.RecipesResponseDto
import org.nikol.roasti.data.recipe.dto.RoastLevelDto
import org.nikol.roasti.domain.recipe.BrewMethod
import org.nikol.roasti.domain.recipe.BrewStep
import org.nikol.roasti.domain.recipe.Difficulty
import org.nikol.roasti.domain.recipe.Recipe
import org.nikol.roasti.domain.recipe.RecipesPaginated
import org.nikol.roasti.domain.recipe.RoastLevel

// DTO → Domain

fun RecipesResponseDto.toDomain() = RecipesPaginated(
    items = items.map { it.toDomain() },
    page = page,
    limit = limit,
    totalCount = totalCount,
)

fun RecipeDto.toDomain(): Recipe = Recipe(
    id = id,
    title = title,
    description = description,
    imageId = imageId,
    brewMethod = brewMethod.toDomain(),
    difficulty = difficulty.toDomain(),
    totalBrewTimeSeconds = steps.orEmpty().sumOf { step -> step.durationSeconds ?: 0 },
    roastLevel = roastLevel?.toDomain(),
    beans = beans,
    steps = steps.orEmpty().map(BrewStepDto::toDomain),
)

fun BrewStepDto.toDomain(): BrewStep = BrewStep(
    order = order,
    title = title,
    description = description,
    durationSeconds = durationSeconds,
    imageId = imageId,
)

fun BrewMethodDto.toDomain(): BrewMethod? = when (this) {
    BrewMethodDto.V60 -> BrewMethod.V60
    BrewMethodDto.FRENCH_PRESS -> BrewMethod.FrenchPress
    BrewMethodDto.AEROPRESS -> BrewMethod.Aeropress
    BrewMethodDto.CHEMEX -> BrewMethod.Chemex
    BrewMethodDto.COLD_BREW -> BrewMethod.ColdBrew
    BrewMethodDto.EXPRESSO_MACHINE -> BrewMethod.EspressoMachine
    BrewMethodDto.MOKA_POT -> BrewMethod.MokaPot
    BrewMethodDto.NONE -> null
}

fun DifficultyDto.toDomain(): Difficulty? = when (this) {
    DifficultyDto.EASY -> Difficulty.Easy
    DifficultyDto.MEDIUM -> Difficulty.Medium
    DifficultyDto.HARD -> Difficulty.Hard
    DifficultyDto.NONE -> null
}

fun RoastLevelDto.toDomain(): RoastLevel? = when (this) {
    RoastLevelDto.LIGHT -> RoastLevel.Light
    RoastLevelDto.MEDIUM_LIGHT -> RoastLevel.MediumLight
    RoastLevelDto.MEDIUM -> RoastLevel.Medium
    RoastLevelDto.MEDIUM_DARK -> RoastLevel.MediumDark
    RoastLevelDto.DARK -> RoastLevel.Dark
    RoastLevelDto.NONE -> null
}

// Domain → DTO

internal fun Recipe.toDto() = RecipeDto(
    id = id,
    authorId = "",
    title = title,
    description = description,
    imageId = imageId,
    brewMethod = brewMethod?.toDto() ?: BrewMethodDto.NONE,
    difficulty = difficulty?.toDto() ?: DifficultyDto.NONE,
    roastLevel = roastLevel.toDto(),
    beans = beans,
    steps = steps.map { it.toDto() },
)

internal fun BrewStep.toDto() = BrewStepDto(
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

internal fun BrewMethod.toDto(): BrewMethodDto = when (this) {
    BrewMethod.V60 -> BrewMethodDto.V60
    BrewMethod.FrenchPress -> BrewMethodDto.FRENCH_PRESS
    BrewMethod.Aeropress -> BrewMethodDto.AEROPRESS
    BrewMethod.Chemex -> BrewMethodDto.CHEMEX
    BrewMethod.ColdBrew -> BrewMethodDto.COLD_BREW
    BrewMethod.EspressoMachine -> BrewMethodDto.EXPRESSO_MACHINE
    BrewMethod.MokaPot -> BrewMethodDto.MOKA_POT
}

internal fun Difficulty.toDto(): DifficultyDto = when (this) {
    Difficulty.Easy -> DifficultyDto.EASY
    Difficulty.Medium -> DifficultyDto.MEDIUM
    Difficulty.Hard -> DifficultyDto.HARD
}
