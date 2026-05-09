package org.nikol.roasti.features.recipes

data class BrewStep(
    val id: Int,
    val title: String,
    val description: String,
    val order: Int,
    val duration: Int,
    val imageId: String?,
)
