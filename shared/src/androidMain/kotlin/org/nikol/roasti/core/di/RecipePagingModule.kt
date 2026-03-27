package org.nikol.roasti.core.di

import org.koin.dsl.module
import org.nikol.roasti.feature.recipe.data.paging.FavoritesRemoteMediator
import org.nikol.roasti.feature.recipe.data.paging.PagingRecipeRepository

val recipePagingModule = module {
    single { FavoritesRemoteMediator(get(), get(), get()) }
    single { PagingRecipeRepository(get(), get(), get(), get(), get()) }
}
