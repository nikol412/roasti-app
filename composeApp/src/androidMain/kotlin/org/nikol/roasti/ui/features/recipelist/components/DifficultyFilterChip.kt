package org.nikol.roasti.ui.features.recipelist.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.nikol.roasti.R
import org.nikol.roasti.feature.recipe.domain.model.Difficulty
import org.nikol.roasti.ui.features.recipe.mapper.labelRes

@Composable
internal fun DifficultyFilterChip(
    selectedDifficulty: Difficulty?,
    onDifficultySelected: (Difficulty?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedLabel = selectedDifficulty?.labelRes()?.let { stringResource(it) }
    val values = Difficulty.entries.associateBy { stringResource(it.labelRes()) }

    FilterChipDropdown(
        modifier = modifier,
        selectedValue = selectedLabel,
        placeholder = stringResource(R.string.recipe_difficulty),
        values = values.map { it.key },
        onSelected = { stringValue ->
            if (stringValue == null) onDifficultySelected(null)
            else onDifficultySelected(values[stringValue])
        }
    )
}
