package org.nikol.roasti.ui.features.profile.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.nikol.roasti.R
import org.nikol.roasti.ui.features.profile.ProfileFavoritesBlock
import org.nikol.roasti.ui.features.recipelist.components.RecipeCompactCard
import org.nikol.roasti.ui.uikit.LoadingStub
import org.nikol.roasti.ui.uikit.TextCard

@Composable
fun ProfileFavoriteRecipesRow(
    item: ProfileFavoritesBlock,
    modifier: Modifier = Modifier,
    horizontalPaddings: Dp = 16.dp
) {
    Column(modifier) {
        Text(
            text = stringResource(R.string.recipe_list_favorite_section_title),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPaddings)
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(horizontal = horizontalPaddings)
        ) {
            if (item == ProfileFavoritesBlock.Empty) {
                item { EmptyCard(Modifier.animateItem()) }
            }

            if (item == ProfileFavoritesBlock.Loading) {
                item {
                    LoadingStub(
                        Modifier
                            .width(200.dp)
                            .height(150.dp)
                            .animateItem()
                    )
                }
            }

            if (item is ProfileFavoritesBlock.Content) {
                items(item.items) {
                    RecipeCompactCard(it, Modifier.animateItem())
                }
            }
        }
    }
}

@Composable
private fun EmptyCard(modifier: Modifier) {
    TextCard(
        text = stringResource(R.string.recipe_list_favorite_empty_state),
        modifier = Modifier
            .size(width = 200.dp, height = 150.dp)
            .then(modifier)
    )

}

@Composable
private fun Content(modifier: Modifier = Modifier) {

}
