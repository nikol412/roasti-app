package org.nikol.roasti.recipe.session

import org.nikol.roasti.recipe.model.BrewStep

data class BrewingSessionState(
    val recipeTitle: String,
    val steps: List<BrewStep>,
    val currentStepIndex: Int,
    val timerState: TimerState,
    val totalSteps: Int,
) {
    val currentStep: BrewStep get() = steps[currentStepIndex]
    val isFirstStep: Boolean get() = currentStepIndex == 0
    val isLastStep: Boolean get() = currentStepIndex == totalSteps - 1
    val progress: Float get() = (currentStepIndex + 1).toFloat() / totalSteps
}

sealed class TimerState {
    data class Idle(val totalSeconds: Int) : TimerState()
    data class Running(val remainingSeconds: Int, val totalSeconds: Int) : TimerState()
    data class Paused(val remainingSeconds: Int, val totalSeconds: Int) : TimerState()
    data object Finished : TimerState()
    data class NoTimer(val autoAdvanceSeconds: Int = AUTO_ADVANCE_SECONDS) : TimerState()

    companion object {
        const val AUTO_ADVANCE_SECONDS = 5
    }
}
