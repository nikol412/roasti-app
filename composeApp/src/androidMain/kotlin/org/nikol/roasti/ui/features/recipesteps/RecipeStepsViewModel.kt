package org.nikol.roasti.ui.features.recipesteps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
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
            runCatching { repository.getById(recipeId) }
                .onSuccess { recipe ->
                    if (recipe == null) {
                        _state.value = RecipeStepsUiState.Error
                    } else {
                        startSession(recipe)
                    }
                }
                .onFailure {
                    _state.value = RecipeStepsUiState.Error
                }
        }
    }

    fun nextStep() {
        val content = currentContent() ?: return
        moveToSession(
            newBrew = content.session.brew.nextStep(),
            shouldAutoStart = content.session.isTimerRunning,
        )
    }

    fun previousStep() {
        val content = currentContent() ?: return
        moveToSession(
            newBrew = content.session.brew.previousStep(),
            shouldAutoStart = content.session.isTimerRunning,
        )
    }

    fun pauseTimer() {
        val nowMillis = timer.nowMillis()
        updateSession { it.copy(timer = it.timer.pause(nowMillis)) }
        stopTicker()
    }

    fun resumeTimer() {
        val nowMillis = timer.nowMillis()
        updateSession { it.copy(timer = it.timer.resume(nowMillis)) }
        startTicker()
    }

    fun finish() {
        _events.tryEmit(RecipeStepsEvent.NavigateBack)
    }

    private fun startSession(recipe: Recipe) {
        val brew = BrewingSession(recipe, currentStepIndex = if (recipe.steps.lastIndex > 0) startStepIndex.coerceIn(0, recipe.steps.lastIndex) else 0)
        startSession(brew)
    }

    private fun startSession(brew: BrewingSession) {
        val nowMillis = timer.nowMillis()
        _state.value = RecipeStepsUiState.Content(
            SessionState(
                brew = brew,
                timer = StepTimerState.forStep(
                    durationSeconds = brew.stepDurationSeconds,
                    isRunning = !brew.isFinished,
                    nowMillis = nowMillis,
                ),
            )
        )
        startTicker()
    }

    private fun startTicker() {
        stopTicker()
        val content = currentContent() ?: return
        if (!content.session.isTimerRunning || content.session.isFinished) return

        timerJob = viewModelScope.launch {
            timer.ticker(TICK_INTERVAL_MILLIS).collect { nowMillis ->
                val current = currentContent() ?: return@collect
                if (!current.session.isTimerRunning) return@collect

                val updatedTimer = current.session.timer.advance(nowMillis)
                if (updatedTimer.remainingMillis <= 0L) {
                    moveToNextStepFromTimer(expectedStepIndex = current.session.currentStepIndex)
                } else {
                    updateSession { it.copy(timer = updatedTimer) }
                }
            }
        }
    }

    private fun stopTicker() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun moveToSession(
        newBrew: BrewingSession,
        shouldAutoStart: Boolean,
    ) {
        stopTicker()

        val nowMillis = timer.nowMillis()
        _state.value = RecipeStepsUiState.Content(
            SessionState(
                brew = newBrew,
                timer = StepTimerState.forStep(
                    durationSeconds = newBrew.stepDurationSeconds,
                    isRunning = shouldAutoStart && !newBrew.isFinished,
                    nowMillis = nowMillis,
                ),
            )
        )

        if (!newBrew.isFinished && shouldAutoStart) {
            startTicker()
        }
    }

    private fun moveToNextStepFromTimer(expectedStepIndex: Int) {
        val content = currentContent() ?: return
        if (content.session.currentStepIndex != expectedStepIndex) return

        moveToSession(
            newBrew = content.session.brew.nextStep(),
            shouldAutoStart = true,
        )
    }

    private fun currentContent() = _state.value as? RecipeStepsUiState.Content

    private fun updateSession(update: (SessionState) -> SessionState) {
        val content = currentContent() ?: return
        _state.value = content.copy(session = update(content.session))
    }

    override fun onCleared() {
        stopTicker()
        super.onCleared()
    }

    private companion object {
        const val TICK_INTERVAL_MILLIS = 50L
    }
}

internal sealed interface RecipeStepsEvent {
    data object NavigateBack : RecipeStepsEvent
}
