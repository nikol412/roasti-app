package org.nikol.roasti.core.di

import org.koin.dsl.module
import org.nikol.roasti.feature.comment.data.paging.PagingCommentRepository

val commentPagingModule = module {
    single { PagingCommentRepository(get(), get()) }
}
