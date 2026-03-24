package org.nikol.roasti.ui.features.recipepage.model

import androidx.annotation.StringRes

data class RecipeDetailsUiModel(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    @StringRes val brewMethodLabelRes: Int,
    @StringRes val difficultyLabelRes: Int,
    @StringRes val roastLevelLabelRes: Int?,
    val beans: String?,
    val steps: List<RecipeStepUiModel>,
    val isLiked: Boolean,
    val likesCount: Int,
)

data class RecipeStepUiModel(
    val order: Int,
    val title: String,
    val description: String,
    val durationSeconds: Int?,
)
