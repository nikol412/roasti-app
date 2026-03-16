package org.nikol.roasti.ui.features.recipesteps

import org.nikol.roasti.domain.recipe.BrewStep
import org.nikol.roasti.domain.recipe.session.BrewingSession
import kotlin.math.ceil

internal sealed interface RecipeStepsUiState {
    data object Loading : RecipeStepsUiState
    data object Error : RecipeStepsUiState
    data class Content(val session: SessionState) : RecipeStepsUiState
}

internal data class StepTimerState(
    val totalMillis: Long,
    val remainingMillis: Long,
    val isRunning: Boolean,
    val startedAtMillis: Long? = null,
) {
    val progress: Float
        get() = if (totalMillis > 0) remainingMillis / totalMillis.toFloat() else 1f

    fun advance(nowMillis: Long): StepTimerState {
        if (!isRunning || startedAtMillis == null) return this

        val updatedRemaining = (remainingMillis - (nowMillis - startedAtMillis)).coerceAtLeast(0L)
        return copy(
            remainingMillis = updatedRemaining,
            startedAtMillis = nowMillis,
        )
    }

    fun pause(nowMillis: Long): StepTimerState {
        val updated = advance(nowMillis)
        return updated.copy(
            isRunning = false,
            startedAtMillis = null,
        )
    }

    fun resume(nowMillis: Long): StepTimerState {
        if (isRunning || remainingMillis <= 0L) return this
        return copy(
            isRunning = true,
            startedAtMillis = nowMillis,
        )
    }

    companion object {
        fun forStep(durationSeconds: Int, isRunning: Boolean, nowMillis: Long): StepTimerState {
            val totalMillis = durationSeconds * MILLIS_IN_SECOND
            return StepTimerState(
                totalMillis = totalMillis,
                remainingMillis = totalMillis,
                isRunning = isRunning,
                startedAtMillis = nowMillis.takeIf { isRunning },
            )
        }

        private const val MILLIS_IN_SECOND = 1000L
    }
}

internal data class SessionState(
    val brew: BrewingSession,
    val timer: StepTimerState,
) {
    val currentStep: BrewStep get() = brew.currentStep
    val currentStepIndex: Int get() = brew.currentStepIndex
    val totalSteps: Int get() = brew.totalSteps
    val isFirstStep: Boolean get() = brew.isFirstStep
    val isLastStep: Boolean get() = brew.isLastStep
    val isFinished: Boolean get() = brew.isFinished
    val stepProgress: Float get() = brew.stepProgress
    val isTimerRunning: Boolean get() = timer.isRunning
    val remainingSeconds: Int get() = ceil(timer.remainingMillis / 1000f).toInt()
    val timerProgress: Float get() = timer.progress
}
