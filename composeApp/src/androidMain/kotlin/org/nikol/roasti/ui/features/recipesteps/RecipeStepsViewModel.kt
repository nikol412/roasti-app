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
import org.nikol.roasti.domain.recipe.RecipeRepository
import org.nikol.roasti.domain.recipe.model.Recipe
import org.nikol.roasti.domain.recipe.session.BrewingSession
import org.nikol.roasti.domain.recipe.session.BrewingTimer
import org.nikol.roasti.ui.features.recipesteps.mapper.toUiState

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
    private var currentBrewingSession: BrewingSession? = null
    private var currentTimerState: StepTimerState? = null

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
        val brew = currentBrewingSession ?: return
        val newBrew = brew.nextStep()
        moveToSession(
            newBrew = newBrew,
            shouldAutoStart = shouldAutoStartTimerFor(newBrew),
        )
    }

    fun previousStep() {
        val brew = currentBrewingSession ?: return
        val newBrew = brew.previousStep()
        moveToSession(
            newBrew = newBrew,
            shouldAutoStart = shouldAutoStartTimerFor(newBrew),
        )
    }

    fun pauseTimer() {
        val nowMillis = timer.nowMillis()
        updateTimer { it.pause(nowMillis) }
        stopTicker()
    }

    fun resumeTimer() {
        val nowMillis = timer.nowMillis()
        updateTimer { it.resume(nowMillis) }
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
        currentBrewingSession = brew
        currentTimerState = StepTimerState.forStep(
            durationSeconds = brew.stepDurationSeconds,
            isRunning = shouldAutoStartTimerFor(brew),
            nowMillis = nowMillis,
        )
        emitContent()
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

                val updatedTimer = currentTimerState?.advance(nowMillis) ?: return@collect
                if (updatedTimer.remainingMillis <= 0L) {
                    stopTicker()
                    currentTimerState = updatedTimer.complete()
                    emitContent()
                } else {
                    currentTimerState = updatedTimer
                    emitContent()
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
        currentBrewingSession = newBrew
        currentTimerState = StepTimerState.forStep(
            durationSeconds = newBrew.stepDurationSeconds,
            isRunning = shouldAutoStart && !newBrew.isFinished,
            nowMillis = nowMillis,
        )
        emitContent()

        if (!newBrew.isFinished && shouldAutoStart) {
            startTicker()
        }
    }

    private fun shouldAutoStartTimerFor(brew: BrewingSession): Boolean {
        val durationSeconds = brew.stepDurationSeconds ?: return false
        return !brew.isFinished && durationSeconds > 0
    }

    private fun currentContent() = _state.value as? RecipeStepsUiState.Content

    private fun updateTimer(update: (StepTimerState) -> StepTimerState) {
        val timerState = currentTimerState ?: return
        currentTimerState = update(timerState)
        emitContent()
    }

    private fun emitContent() {
        val brew = currentBrewingSession ?: return
        val timerState = currentTimerState ?: return
        _state.value = RecipeStepsUiState.Content(
            session = brew.toUiState(timer = timerState),
        )
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
