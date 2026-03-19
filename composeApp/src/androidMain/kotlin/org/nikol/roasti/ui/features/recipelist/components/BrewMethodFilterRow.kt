package org.nikol.roasti.ui.features.recipelist.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.nikol.roasti.R
import org.nikol.roasti.domain.recipe.model.BrewMethod
import org.nikol.roasti.ui.features.recipe.mapper.labelRes
import org.nikol.roasti.ui.theme.Spacing

@Composable
internal fun BrewMethodFilterRow(
    selectedMethod: BrewMethod?,
    onMethodSelected: (BrewMethod) -> Unit,
    modifier: Modifier = Modifier,
) {
    val methods = remember {
        listOf(BrewMethod.NONE) + BrewMethod.entries.filter { it != BrewMethod.NONE }
    }

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        contentPadding = PaddingValues(horizontal = Spacing.lg),
    ) {
        items(methods, key = { it.name }) { method ->
            val isSelected = if (method == BrewMethod.NONE) {
                selectedMethod == null
            } else {
                selectedMethod == method
            }
            FilterChip(
                selected = isSelected,
                onClick = { onMethodSelected(method) },
                label = {
                    Text(
                        text = if (method == BrewMethod.NONE) {
                            stringResource(R.string.recipe_brew_method_all)
                        } else {
                            stringResource(method.labelRes())
                        },
                    )
                },
            )
        }
    }
}
