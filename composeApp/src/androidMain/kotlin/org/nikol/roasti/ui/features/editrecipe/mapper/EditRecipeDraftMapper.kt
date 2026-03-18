package org.nikol.roasti.ui.features.editrecipe.mapper

import org.nikol.roasti.domain.recipe.model.RecipeDraft
import org.nikol.roasti.ui.features.editrecipe.model.EditRecipeUiState
import org.nikol.roasti.ui.features.recipeform.mapper.toRecipeDraft

internal fun EditRecipeUiState.toRecipeDraft(): RecipeDraft = form.toRecipeDraft()
