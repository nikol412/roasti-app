package org.nikol.roasti.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.nikol.roasti.ui.features.recipelist.RecipesListScreen

@Composable
fun RecipesScreen(onRecipeClick: (String) -> Unit = {}) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        RecipesListScreen(onRecipeClick = { onRecipeClick(it.id) })
    }
}
