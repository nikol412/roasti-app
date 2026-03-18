package org.nikol.roasti.ui.features.editrecipe.model

data class EditRecipeStepUiModel(
    val order: Int,
    val title: String,
    val description: String,
    val durationSeconds: Int?,
    val imageId: String? = null,
)
