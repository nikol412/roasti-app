package org.nikol.roasti.ui.features.editrecipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nikol.roasti.feature.recipe.domain.RecipeRepository
import org.nikol.roasti.feature.recipe.domain.model.BrewMethod
import org.nikol.roasti.feature.recipe.domain.model.Difficulty
import org.nikol.roasti.feature.recipe.domain.model.RoastLevel
import org.nikol.roasti.feature.upload.domain.UploadRepository
import org.nikol.roasti.ui.features.editrecipe.mapper.toEditState
import org.nikol.roasti.ui.features.editrecipe.mapper.toRecipeDraft
import org.nikol.roasti.ui.features.editrecipe.model.EditRecipeEvent
import org.nikol.roasti.ui.features.editrecipe.model.EditRecipeUiState
import org.nikol.roasti.ui.features.recipeform.model.ActiveStepSheet
import org.nikol.roasti.ui.features.recipeform.model.RecipeFormFields
import org.nikol.roasti.ui.features.recipeform.model.RecipeFormStepUiModel
import org.nikol.roasti.core.utils.imageUrl

class EditRecipeViewModel(
    private val recipeId: String,
    private val recipeRepository: RecipeRepository,
    private val uploadRepository: UploadRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(EditRecipeUiState())
    val state: StateFlow<EditRecipeUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<EditRecipeEvent>()
    val events: SharedFlow<EditRecipeEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            val recipe = recipeRepository.getById(recipeId).getOrNull()
            _state.update {
                recipe?.toEditState() ?: it.copy(isLoading = false, loadError = true)
            }
        }
    }

    fun updateTitle(value: String) = _state.update { it.copyForm { copy(title = value, saveError = false) } }
    fun updateDescription(value: String) = _state.update { it.copyForm { copy(description = value) } }
    fun updateBrewMethod(value: BrewMethod) = _state.update { it.copyForm { copy(brewMethod = value) } }
    fun updateDifficulty(value: Difficulty) = _state.update { it.copyForm { copy(difficulty = value) } }
    fun updateRoastLevel(value: RoastLevel) = _state.update { it.copyForm { copy(roastLevel = value) } }
    fun updateBeans(value: String) = _state.update { it.copyForm { copy(beans = value) } }

    fun openAddStep() = _state.update { it.copyForm { copy(activeStepSheet = ActiveStepSheet(editingIndex = null)) } }

    fun openEditStep(index: Int) {
        val step = _state.value.form.steps.getOrNull(index) ?: return
        _state.update {
            it.copyForm {
                copy(
                    activeStepSheet = ActiveStepSheet(
                        editingIndex = index,
                        title = step.title,
                        durationMinutes = step.durationSeconds?.let { s -> (s / 60).toString() } ?: "",
                        durationSeconds = step.durationSeconds?.let { s -> (s % 60).toString() } ?: "",
                    )
                )
            }
        }
    }

    fun updateActiveStepTitle(value: String) =
        _state.update { it.copyForm { copy(activeStepSheet = activeStepSheet?.copy(title = value)) } }

    fun updateActiveStepDurationMinutes(value: String) =
        _state.update { it.copyForm { copy(activeStepSheet = activeStepSheet?.copy(durationMinutes = value)) } }

    fun updateActiveStepDurationSeconds(value: String) =
        _state.update { it.copyForm { copy(activeStepSheet = activeStepSheet?.copy(durationSeconds = value)) } }

    fun confirmStepEdit() {
        val sheet = _state.value.form.activeStepSheet ?: return
        if (!sheet.canConfirm) return
        val newStep = RecipeFormStepUiModel(
            order = sheet.editingIndex ?: _state.value.form.steps.size,
            title = sheet.title,
            durationSeconds = sheet.durationTotalSeconds,
        )
        _state.update { state ->
            state.copyForm {
                val updatedSteps = steps.toMutableList()
                val idx = sheet.editingIndex
                if (idx != null) updatedSteps[idx] = newStep else updatedSteps.add(newStep)
                copy(steps = updatedSteps, activeStepSheet = null)
            }
        }
    }

    fun cancelStepEdit() = _state.update { it.copyForm { copy(activeStepSheet = null) } }

    fun removeStep(index: Int) = _state.update { state ->
        state.copyForm { copy(steps = steps.toMutableList().also { it.removeAt(index) }) }
    }

    fun uploadImage(fileName: String, bytes: ByteArray) {
        viewModelScope.launch {
            _state.update { it.copyForm { copy(isUploadingImage = true) } }
            val result = uploadRepository.uploadImage(fileName, bytes)
            if (result.isFailure) _events.emit(EditRecipeEvent.ImageUploadFailed)
            _state.update { state ->
                state.copyForm {
                    copy(
                        imageId = result.getOrNull()?.id ?: imageId,
                        imageUrl = result.getOrNull()?.id?.let(::imageUrl) ?: imageUrl,
                        isUploadingImage = false,
                    )
                }
            }
        }
    }

    fun save() {
        viewModelScope.launch {
            _state.update { it.copyForm { copy(isSaving = true, saveError = false) } }
            val draft = state.value.toRecipeDraft()
            val result = recipeRepository.updateRecipe(recipeId, draft)
            if (result.isSuccess) {
                _events.emit(EditRecipeEvent.SaveSuccess)
            } else {
                _state.update { it.copyForm { copy(isSaving = false, saveError = true) } }
                _events.emit(EditRecipeEvent.SaveError)
            }
        }
    }
}

private fun EditRecipeUiState.copyForm(transform: RecipeFormFields.() -> RecipeFormFields) =
    copy(form = form.transform())
