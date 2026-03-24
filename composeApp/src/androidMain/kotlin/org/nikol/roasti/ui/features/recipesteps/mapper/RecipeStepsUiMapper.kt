package org.nikol.roasti.ui.features.recipesteps.mapper

import org.nikol.roasti.feature.recipe.domain.model.BrewStep
import org.nikol.roasti.feature.recipe.domain.session.BrewingSession
import org.nikol.roasti.ui.features.recipesteps.BrewingStepUiModel
import org.nikol.roasti.ui.features.recipesteps.SessionState
import org.nikol.roasti.ui.features.recipesteps.StepTimerState

internal fun BrewingSession.toUiState(timer: StepTimerState) = SessionState(
    steps = recipe.steps.map(BrewStep::toUiModel),
    currentStepIndex = currentStepIndex,
    totalSteps = totalSteps,
    isFirstStep = isFirstStep,
    isLastStep = isLastStep,
    isFinished = isFinished,
    stepProgress = stepProgress,
    timer = timer,
)

private fun BrewStep.toUiModel() = BrewingStepUiModel(
    order = order,
    title = title,
    description = description,
    durationSeconds = durationSeconds,
)
