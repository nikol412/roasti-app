package org.nikol.roasti.ui.features.recipelist.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.nikol.roasti.R
import org.nikol.roasti.domain.recipe.model.Difficulty
import org.nikol.roasti.ui.features.recipe.mapper.labelRes

@Composable
internal fun DifficultyFilterChip(
    selectedDifficulty: Difficulty?,
    onDifficultySelected: (Difficulty?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val difficulties = remember { Difficulty.entries }
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        FilterChip(
            selected = selectedDifficulty != null,
            onClick = { expanded = true },
            label = {
                Text(
                    text = if (selectedDifficulty != null) {
                        stringResource(selectedDifficulty.labelRes())
                    } else {
                        stringResource(R.string.recipe_difficulty)
                    },
                )
            },
            trailingIcon = {
                Text(if (expanded) "▲" else "▼")
            },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            difficulties.forEach { difficulty ->
                DropdownMenuItem(
                    text = { Text(stringResource(difficulty.labelRes())) },
                    onClick = {
                        onDifficultySelected(difficulty)
                        expanded = false
                    },
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text(stringResource(R.string.filter_clear)) },
                onClick = {
                    onDifficultySelected(null)
                    expanded = false
                },
            )
        }
    }
}
