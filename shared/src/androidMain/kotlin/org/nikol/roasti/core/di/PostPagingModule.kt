package org.nikol.roasti.core.di

import org.koin.dsl.module
import org.nikol.roasti.feature.post.data.paging.AllPostsRemoteMediator
import org.nikol.roasti.feature.post.data.paging.PagingPostRepository

val postPagingModule = module {
    single { AllPostsRemoteMediator(get(), get()) }
    single { PagingPostRepository(get(), get(), get()) }
}
