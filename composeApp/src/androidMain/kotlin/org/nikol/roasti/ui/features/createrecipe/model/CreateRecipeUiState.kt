package org.nikol.roasti.ui.features.createrecipe.model

import org.nikol.roasti.feature.recipe.domain.model.BrewMethod
import org.nikol.roasti.feature.recipe.domain.model.Difficulty
import org.nikol.roasti.feature.recipe.domain.model.RoastLevel

data class CreateRecipeStepUiModel(
    val title: String,
    val description: String,
    val durationInSeconds: Int = 0,
    val imageId: String? = null,
)

data class CreateRecipeUiState(
    val name: String = "",
    val brewMethod: BrewMethod? = null,
    val description: String = "",
    val difficulty: Difficulty = Difficulty.Medium,
    val imageId: String? = null,
    val isUploadingImage: Boolean = false,
    val pendingStepImageId: String? = null,
    val isUploadingStepImage: Boolean = false,
    val roastLevel: RoastLevel? = null,
    val beans: String = "",
    val brewSteps: List<CreateRecipeStepUiModel> = emptyList(),
) {
    val isDirty: Boolean
        get() = name.isNotBlank() || brewMethod != null || beans.isNotBlank() || description.isNotBlank()

    val canContinueToSteps: Boolean
        get() = name.isNotBlank() && brewMethod != null && roastLevel != null
}
