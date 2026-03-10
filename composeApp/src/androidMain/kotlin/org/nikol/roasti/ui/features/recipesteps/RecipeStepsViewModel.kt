package org.nikol.roasti.ui.features.recipesteps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.nikol.roasti.recipe.model.Recipe
import org.nikol.roasti.recipe.repository.RecipeRepository
import org.nikol.roasti.recipe.session.BrewingSession
import org.nikol.roasti.recipe.session.BrewingTimer

internal class RecipeStepsViewModel(
    private val recipeId: String,
    private val startStepIndex: Int,
    private val repository: RecipeRepository,
    private val timer: BrewingTimer,
) : ViewModel() {

    private val _state = MutableStateFlow<RecipeStepsUiState>(RecipeStepsUiState.Loading)
    val state: StateFlow<RecipeStepsUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<RecipeStepsEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<RecipeStepsEvent> = _events.asSharedFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            val recipe = repository.getById(recipeId)
            if (recipe == null) _state.value = RecipeStepsUiState.Error
            else startSession(recipe)
        }
    }

    fun nextStep() {
        val content = currentContent() ?: return
        timerJob?.cancel()
        val newBrew = content.session.brew.nextStep()
        val newTotal = newBrew.stepDurationSeconds
        _state.value = content.copy(
            session = content.session.copy(
                brew = newBrew,
                remainingSeconds = newTotal,
                totalSeconds = newTotal,
                isTimerRunning = !newBrew.isFinished,
            )
        )
        if (!newBrew.isFinished) startTimer()
    }

    fun previousStep() {
        val content = currentContent() ?: return
        timerJob?.cancel()
        val newBrew = content.session.brew.previousStep()
        val newTotal = newBrew.stepDurationSeconds
        _state.value = content.copy(
            session = content.session.copy(
                brew = newBrew,
                remainingSeconds = newTotal,
                totalSeconds = newTotal,
                isTimerRunning = true,
            )
        )
        startTimer()
    }

    fun pauseTimer() {
        timerJob?.cancel()
        updateSession { it.copy(isTimerRunning = false) }
    }

    fun resumeTimer() {
        updateSession { it.copy(isTimerRunning = true) }
        startTimer()
    }

    fun finish() {
        _events.tryEmit(RecipeStepsEvent.NavigateBack)
    }

    private fun startSession(recipe: Recipe) {
        val brew = BrewingSession(recipe, currentStepIndex = startStepIndex.coerceIn(0, recipe.steps.lastIndex))
        return startSession(brew)
    }

    private fun startSession(brew: BrewingSession) {
        val total = brew.stepDurationSeconds
        _state.value = RecipeStepsUiState.Content(
            SessionState(brew = brew, remainingSeconds = total, totalSeconds = total, isTimerRunning = true)
        )
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        val content = currentContent() ?: return
        timerJob = viewModelScope.launch {
            timer.countdown(content.session.brew.stepDurationSeconds).collect { remaining ->
                if (remaining == 0) {
                    delay(300L)
                    nextStep()
                } else {
                    updateSession { it.copy(remainingSeconds = remaining) }
                }
            }
        }
    }

    private fun currentContent() = _state.value as? RecipeStepsUiState.Content

    private fun updateSession(update: (SessionState) -> SessionState) {
        val content = currentContent() ?: return
        _state.value = content.copy(session = update(content.session))
    }
}

internal sealed interface RecipeStepsEvent {
    data object NavigateBack : RecipeStepsEvent
}
