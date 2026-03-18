package org.nikol.roasti.ui.features.editrecipe.model

import org.nikol.roasti.domain.recipe.model.BrewMethod
import org.nikol.roasti.domain.recipe.model.Difficulty
import org.nikol.roasti.domain.recipe.model.RoastLevel

data class EditRecipeUiState(
    val isLoading: Boolean = true,
    val loadError: Boolean = false,
    val title: String = "",
    val description: String = "",
    val imageId: String? = null,
    val imageUrl: String? = null,
    val brewMethod: BrewMethod = BrewMethod.NONE,
    val difficulty: Difficulty = Difficulty.Medium,
    val roastLevel: RoastLevel = RoastLevel.NONE,
    val beans: String = "",
    val steps: List<EditRecipeStepUiModel> = emptyList(),
    val isUploadingImage: Boolean = false,
    val isSaving: Boolean = false,
    val saveError: Boolean = false,
    val activeStepSheet: ActiveStepSheet? = null,
) {
    val canSave: Boolean get() = title.isNotBlank() && !isLoading && !isSaving
    val isEditing: Boolean get() = !isLoading && !loadError
}

data class ActiveStepSheet(
    val editingIndex: Int?,
    val title: String = "",
    val description: String = "",
    val durationMinutes: String = "",
    val durationSeconds: String = "",
) {
    val canConfirm: Boolean get() = title.isNotBlank()

    val durationTotalSeconds: Int?
        get() {
            val mins = durationMinutes.toIntOrNull() ?: 0
            val secs = durationSeconds.toIntOrNull() ?: 0
            return if (mins == 0 && secs == 0) null else mins * 60 + secs
        }
}
