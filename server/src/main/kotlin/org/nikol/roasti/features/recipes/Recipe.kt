package org.nikol.roasti.features.recipes

import org.nikol.roasti.feature.recipe.domain.model.BrewMethod
import org.nikol.roasti.feature.recipe.domain.model.Difficulty
import org.nikol.roasti.feature.recipe.domain.model.RoastLevel
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class Recipe(
    val id: Uuid,
    val title: String,
    val description: String,
    val imageUrl: String,
    val brewMethod: BrewMethod,
    val difficulty: Difficulty,
    val roastLevel: RoastLevel,
    val public: Boolean,
    val steps: List<BrewStep>,
    val createdAt: Instant,
    val updatedAt: Instant,
)
