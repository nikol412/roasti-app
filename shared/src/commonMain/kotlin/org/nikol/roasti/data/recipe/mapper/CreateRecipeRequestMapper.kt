package org.nikol.roasti.data.recipe.mapper

import org.nikol.roasti.data.recipe.remote.model.BrewMethodDto
import org.nikol.roasti.data.recipe.remote.model.DifficultyDto
import org.nikol.roasti.data.recipe.remote.model.RoastLevelDto
import org.nikol.roasti.data.recipe.remote.model.request.CreateRecipeRequestDto
import org.nikol.roasti.data.recipe.remote.model.request.CreateRecipeStepRequestDto
import org.nikol.roasti.domain.recipe.model.BrewMethod
import org.nikol.roasti.domain.recipe.model.Difficulty
import org.nikol.roasti.domain.recipe.model.RecipeDraft
import org.nikol.roasti.domain.recipe.model.RecipeDraftStep
import org.nikol.roasti.domain.recipe.model.RoastLevel

fun RecipeDraft.toRequestDto() = CreateRecipeRequestDto(
    title = title,
    beans = beans,
    brewMethod = brewMethod.toRequestDto(),
    description = description,
    difficulty = difficulty.toRequestDto(),
    imageId = imageId,
    roastLevel = roastLevel.toRequestDto(),
    steps = steps.map { it.toRequestDto() },
)

fun RecipeDraftStep.toRequestDto() = CreateRecipeStepRequestDto(
    description = description,
    durationSeconds = durationSeconds,
    imageId = imageId,
    order = order,
    title = title,
)

fun BrewMethod?.toRequestDto(): BrewMethodDto = when (this) {
    BrewMethod.V60 -> BrewMethodDto.V60
    BrewMethod.FrenchPress -> BrewMethodDto.FRENCH_PRESS
    BrewMethod.Aeropress -> BrewMethodDto.AEROPRESS
    BrewMethod.Chemex -> BrewMethodDto.CHEMEX
    BrewMethod.ColdBrew -> BrewMethodDto.COLD_BREW
    BrewMethod.EspressoMachine -> BrewMethodDto.EXPRESSO_MACHINE
    BrewMethod.MokaPot -> BrewMethodDto.MOKA_POT
    BrewMethod.NONE, null -> BrewMethodDto.NONE
}

fun Difficulty.toRequestDto(): DifficultyDto = when (this) {
    Difficulty.Easy -> DifficultyDto.EASY
    Difficulty.Medium -> DifficultyDto.MEDIUM
    Difficulty.Hard -> DifficultyDto.HARD
}

fun RoastLevel?.toRequestDto(): RoastLevelDto = when (this) {
    RoastLevel.Light -> RoastLevelDto.LIGHT
    RoastLevel.MediumLight -> RoastLevelDto.MEDIUM_LIGHT
    RoastLevel.Medium -> RoastLevelDto.MEDIUM
    RoastLevel.MediumDark -> RoastLevelDto.MEDIUM_DARK
    RoastLevel.Dark -> RoastLevelDto.DARK
    RoastLevel.NONE, null -> RoastLevelDto.NONE
}

fun Difficulty?.toQueryDto(): DifficultyDto? = when (this) {
    Difficulty.Easy -> DifficultyDto.EASY
    Difficulty.Medium -> DifficultyDto.MEDIUM
    Difficulty.Hard -> DifficultyDto.HARD
    null -> null
}

fun RoastLevel?.toQueryDto(): RoastLevelDto? = when (this) {
    RoastLevel.Light -> RoastLevelDto.LIGHT
    RoastLevel.MediumLight -> RoastLevelDto.MEDIUM_LIGHT
    RoastLevel.Medium -> RoastLevelDto.MEDIUM
    RoastLevel.MediumDark -> RoastLevelDto.MEDIUM_DARK
    RoastLevel.Dark -> RoastLevelDto.DARK
    RoastLevel.NONE, null -> null
}
