package org.nikol.roasti.ui.features.recipepage

sealed interface RecipeContentNavEvent {
    data object NavigateBack : RecipeContentNavEvent
}
