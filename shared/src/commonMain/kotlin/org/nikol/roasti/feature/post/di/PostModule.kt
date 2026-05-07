package org.nikol.roasti.feature.post.di

import org.koin.dsl.module
import org.nikol.roasti.core.config.AppConfig
import org.nikol.roasti.feature.auth.domain.model.AuthState
import org.nikol.roasti.feature.auth.domain.repository.AuthRepository
import org.nikol.roasti.feature.post.data.network.CurrentMockAuthor
import org.nikol.roasti.feature.post.data.network.MockPostsApiClient
import org.nikol.roasti.feature.post.data.network.PostsApiClient
import org.nikol.roasti.feature.post.data.network.PostsApiClientImpl
import org.nikol.roasti.feature.post.data.remote.model.response.PostAuthorDto

val postModule = module {
    single<PostsApiClient> {
        if (AppConfig.USE_MOCK_POSTS_API) {
            val authRepository: AuthRepository = get()
            MockPostsApiClient(
                currentAuthorProvider = {
                    val user = (authRepository.authState.value as? AuthState.Authenticated)?.user
                    if (user != null) {
                        PostAuthorDto(
                            id = user.id,
                            username = user.username,
                            avatarId = user.avatarId,
                        )
                    } else {
                        CurrentMockAuthor
                    }
                },
            )
        } else {
            PostsApiClientImpl(httpClient = get(), authorizedRequestExecutor = get())
        }
    }
}
