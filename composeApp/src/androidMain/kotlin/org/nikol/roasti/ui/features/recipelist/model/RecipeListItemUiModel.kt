package org.nikol.roasti.ui.features.recipelist.model

import androidx.annotation.StringRes

data class RecipeListItemUiModel(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    @StringRes val brewMethodLabelRes: Int,
    @StringRes val difficultyLabelRes: Int,
    val isLiked: Boolean,
    val likesCount: Int,
)
