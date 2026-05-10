package org.nikol.roasti.core.di

import org.koin.dsl.bind
import org.koin.dsl.module
import org.nikol.roasti.feature.recipe.data.RecipeListsRepositoryImpl
import org.nikol.roasti.feature.recipe.data.paging.FavoritesRemoteMediator
import org.nikol.roasti.feature.recipe.domain.RecipeListsRepository

val recipePagingModule = module {
    single { FavoritesRemoteMediator(get(), get(), get()) }
    single { RecipeListsRepositoryImpl(get(), get(), get(), get()) } bind RecipeListsRepository::class
}
