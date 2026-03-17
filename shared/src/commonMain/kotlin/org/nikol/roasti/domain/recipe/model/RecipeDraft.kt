package org.nikol.roasti.domain.recipe.model

data class RecipeDraft(
    val title: String,
    val description: String,
    val imageId: String?,
    val brewMethod: BrewMethod,
    val difficulty: Difficulty,
    val roastLevel: RoastLevel,
    val beans: String?,
    val steps: List<RecipeDraftStep>,
)

data class RecipeDraftStep(
    val order: Int,
    val title: String,
    val description: String?,
    val durationSeconds: Int?,
    val imageId: String? = null,
)
