package org.nikol.roasti.feature.comment.di

import org.koin.dsl.module
import org.nikol.roasti.core.config.AppConfig
import org.nikol.roasti.feature.comment.data.network.CommentsApiClient
import org.nikol.roasti.feature.comment.data.network.CommentsApiClientImpl
import org.nikol.roasti.feature.comment.data.network.MockCommentsApiClient

val commentModule = module {
    single<CommentsApiClient> {
        if (AppConfig.USE_MOCK_COMMENTS_API) {
            MockCommentsApiClient()
        } else {
            CommentsApiClientImpl(httpClient = get(), authorizedRequestExecutor = get())
        }
    }
}
