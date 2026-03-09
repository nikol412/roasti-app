package org.nikol.roasti.recipe.model

data class BrewStep(
    val order: Int,
    val title: String,
    val description: String,
    val durationSeconds: Int?,
)
