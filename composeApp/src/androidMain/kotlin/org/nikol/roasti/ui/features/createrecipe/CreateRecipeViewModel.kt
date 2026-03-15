package org.nikol.roasti.ui.features.createrecipe

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
import org.nikol.roasti.recipe.model.BrewMethod
import org.nikol.roasti.recipe.model.BrewStep
import org.nikol.roasti.recipe.model.Difficulty
import org.nikol.roasti.recipe.model.Recipe
import org.nikol.roasti.recipe.model.RoastLevel
import org.nikol.roasti.recipe.repository.RecipeRepository
import org.nikol.roasti.upload.repository.UploadRepository


data class CreateRecipeFormBrewStepItem(
    val title: String,
    val description: String,
    val durationInSeconds: Int = 0,
    val imageId: String? = null,
)

data class CreateRecipeFormState(
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
    val brewSteps: List<CreateRecipeFormBrewStepItem> = emptyList(),
) {

    val isDirty: Boolean
        get() = name.isNotBlank() || brewMethod != null || beans.isNotBlank() || description.isNotBlank()

    val canContinueToSteps: Boolean
        get() = name.isNotBlank() && brewMethod != null
}

sealed interface CreateRecipeEvent {
    data class OnRequestFinished(val recipe: Recipe?) : CreateRecipeEvent
    data object OnImageUploadFailed : CreateRecipeEvent
}

class CreateRecipeViewModel(
    private val repository: RecipeRepository,
    private val uploadRepository: UploadRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(CreateRecipeFormState())
    val state: StateFlow<CreateRecipeFormState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<CreateRecipeEvent>()

    val events: SharedFlow<CreateRecipeEvent> = _events.asSharedFlow()

    fun updateName(value: String) = _state.update { it.copy(name = value) }
    fun updateBrewMethod(value: BrewMethod?) = _state.update { it.copy(brewMethod = value) }
    fun updateBeans(value: String) = _state.update { it.copy(beans = value) }
    fun updateDifficulty(value: Difficulty) = _state.update { it.copy(difficulty = value) }
    fun updateDescription(value: String) = _state.update { it.copy(description = value) }
    fun addBrewStep(step: CreateRecipeFormBrewStepItem) {
        _state.update { it.copy(brewSteps = it.brewSteps + step, pendingStepImageId = null) }
    }

    fun removeBrewStepByIndex(index: Int) {
        _state.update {
            val updatedList = it.brewSteps.toMutableList()
            updatedList.removeAt(index)
            it.copy(brewSteps = updatedList)
        }
    }

    fun uploadImage(fileName: String, bytes: ByteArray) {
        viewModelScope.launch {
            _state.update { it.copy(isUploadingImage = true) }
            val result = uploadRepository.uploadImage(fileName, bytes)
            if (result.isFailure) _events.emit(CreateRecipeEvent.OnImageUploadFailed)
            _state.update { it.copy(imageId = result.getOrNull()?.id, isUploadingImage = false) }
        }
    }

    fun uploadBrewStepImage(fileName: String, bytes: ByteArray) {
        viewModelScope.launch {
            _state.update { it.copy(isUploadingStepImage = true) }
            val result = uploadRepository.uploadImage(fileName, bytes)
            if (result.isFailure) _events.emit(CreateRecipeEvent.OnImageUploadFailed)
            _state.update { it.copy(pendingStepImageId = result.getOrNull()?.id, isUploadingStepImage = false) }
        }
    }

    fun reset() = _state.update { CreateRecipeFormState() }

    fun publishRecipe() {
        val recipe = state.value.toRecipe()
        viewModelScope.launch {
            val result = repository.addRecipe(recipe)
            _events.emit(CreateRecipeEvent.OnRequestFinished(result.getOrNull()))
        }
    }
}


private fun CreateRecipeFormState.toRecipe() = Recipe(
    "",
    title = this.name,
    description = description,
    imageId = imageId,
    brewMethod = brewMethod,
    difficulty = difficulty,
    totalBrewTimeSeconds = 0,
    roastLevel = roastLevel,
    beans = beans,
    steps = brewSteps.mapIndexed { index, item -> item.toBrewStep(index) },
)

private fun CreateRecipeFormBrewStepItem.toBrewStep(index: Int) =
    BrewStep(index, title, description, durationInSeconds, imageId)