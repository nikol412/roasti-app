package org.nikol.roasti.domain.recipe.model

data class BrewStep(
    val order: Int,
    val title: String,
    val description: String,
    val durationSeconds: Int?,
    val imageId: String? = null,
)
