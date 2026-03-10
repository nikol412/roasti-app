package org.nikol.roasti.ui.features.recipesteps

import org.nikol.roasti.recipe.model.BrewStep
import org.nikol.roasti.recipe.session.BrewingSession

internal sealed interface RecipeStepsUiState {
    data object Loading : RecipeStepsUiState
    data object Error : RecipeStepsUiState
    data class Content(val session: SessionState) : RecipeStepsUiState
}

internal data class SessionState(
    val brew: BrewingSession,
    val remainingSeconds: Int,
    val totalSeconds: Int,
    val isTimerRunning: Boolean,
) {
    val currentStep: BrewStep get() = brew.currentStep
    val currentStepIndex: Int get() = brew.currentStepIndex
    val totalSteps: Int get() = brew.totalSteps
    val isFirstStep: Boolean get() = brew.isFirstStep
    val isLastStep: Boolean get() = brew.isLastStep
    val isFinished: Boolean get() = brew.isFinished
    val stepProgress: Float get() = brew.stepProgress
    val timerProgress: Float get() = if (totalSeconds > 0) remainingSeconds / totalSeconds.toFloat() else 1f
}
