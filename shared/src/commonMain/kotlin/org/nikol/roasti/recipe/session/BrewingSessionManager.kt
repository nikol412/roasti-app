package org.nikol.roasti.recipe.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.nikol.roasti.recipe.model.Recipe

class BrewingSessionManager {

    private val _state = MutableStateFlow<BrewingSessionState?>(null)
    val state: StateFlow<BrewingSessionState?> = _state.asStateFlow()

    private var timerJob: Job? = null

    fun start(recipe: Recipe) {
        timerJob?.cancel()
        _state.value = BrewingSessionState(
            recipeTitle = recipe.title,
            steps = recipe.steps,
            currentStepIndex = 0,
            timerState = initialTimerState(recipe.steps.first().durationSeconds),
            totalSteps = recipe.steps.size,
        )
    }

    fun startTimer(scope: CoroutineScope) {
        val current = _state.value ?: return
        val timerState = current.timerState

        val totalSeconds: Int
        val remainingSeconds: Int

        when (timerState) {
            is TimerState.Idle -> {
                totalSeconds = timerState.totalSeconds
                remainingSeconds = totalSeconds
            }
            is TimerState.Paused -> {
                totalSeconds = timerState.totalSeconds
                remainingSeconds = timerState.remainingSeconds
            }
            is TimerState.NoTimer -> {
                startAutoAdvance(scope, timerState.autoAdvanceSeconds)
                return
            }
            else -> return
        }

        timerJob?.cancel()
        timerJob = scope.launch {
            var remaining = remainingSeconds
            _state.value = current.copy(
                timerState = TimerState.Running(remaining, totalSeconds)
            )
            while (remaining > 0) {
                delay(1000L)
                remaining--
                _state.value = _state.value?.copy(
                    timerState = TimerState.Running(remaining, totalSeconds)
                )
            }
            _state.value = _state.value?.copy(timerState = TimerState.Finished)
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        val current = _state.value ?: return
        val running = current.timerState as? TimerState.Running ?: return
        _state.value = current.copy(
            timerState = TimerState.Paused(running.remainingSeconds, running.totalSeconds)
        )
    }

    fun resetTimer() {
        timerJob?.cancel()
        val current = _state.value ?: return
        _state.value = current.copy(
            timerState = initialTimerState(current.currentStep.durationSeconds)
        )
    }

    fun nextStep() {
        timerJob?.cancel()
        val current = _state.value ?: return
        if (current.isLastStep) return
        val nextIndex = current.currentStepIndex + 1
        _state.value = current.copy(
            currentStepIndex = nextIndex,
            timerState = initialTimerState(current.steps[nextIndex].durationSeconds),
        )
    }

    fun previousStep() {
        timerJob?.cancel()
        val current = _state.value ?: return
        if (current.isFirstStep) return
        val prevIndex = current.currentStepIndex - 1
        _state.value = current.copy(
            currentStepIndex = prevIndex,
            timerState = initialTimerState(current.steps[prevIndex].durationSeconds),
        )
    }

    fun finish() {
        timerJob?.cancel()
        _state.value = null
    }

    private fun startAutoAdvance(scope: CoroutineScope, seconds: Int) {
        timerJob?.cancel()
        timerJob = scope.launch {
            val current = _state.value ?: return@launch
            var remaining = seconds
            _state.value = current.copy(
                timerState = TimerState.Running(remaining, seconds)
            )
            while (remaining > 0) {
                delay(1000L)
                remaining--
                _state.value = _state.value?.copy(
                    timerState = TimerState.Running(remaining, seconds)
                )
            }
            _state.value = _state.value?.copy(timerState = TimerState.Finished)
        }
    }

    private fun initialTimerState(durationSeconds: Int?): TimerState {
        return if (durationSeconds != null) {
            TimerState.Idle(durationSeconds)
        } else {
            TimerState.NoTimer()
        }
    }
}
