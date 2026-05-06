package org.nikol.roasti.feature.post.di

import org.koin.dsl.module
import org.nikol.roasti.core.config.AppConfig
import org.nikol.roasti.feature.post.data.network.MockPostsApiClient
import org.nikol.roasti.feature.post.data.network.PostsApiClient
import org.nikol.roasti.feature.post.data.network.PostsApiClientImpl

val postModule = module {
    single<PostsApiClient> {
        if (AppConfig.USE_MOCK_POSTS_API) {
            MockPostsApiClient()
        } else {
            PostsApiClientImpl(httpClient = get(), authorizedRequestExecutor = get())
        }
    }
}
