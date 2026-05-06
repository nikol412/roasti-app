package org.nikol.roasti.feature.post.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.nikol.roasti.core.network.ApiRoutes
import org.nikol.roasti.core.network.AuthorizedRequestExecutor
import org.nikol.roasti.feature.post.data.remote.model.request.VoteRequestDto
import org.nikol.roasti.feature.post.data.remote.model.response.PostResponseDto
import org.nikol.roasti.feature.post.data.remote.model.response.PostVoteResponseDto
import org.nikol.roasti.feature.post.data.remote.model.response.PostsPageResponseDto

interface PostsApiClient {
    suspend fun getPosts(
        page: Int,
        limit: Int,
        query: String? = null,
    ): Result<PostsPageResponseDto>

    suspend fun getPost(id: String): Result<PostResponseDto>

    suspend fun vote(id: String, request: VoteRequestDto): Result<PostVoteResponseDto>
}

class PostsApiClientImpl(
    private val httpClient: HttpClient,
    private val authorizedRequestExecutor: AuthorizedRequestExecutor,
) : PostsApiClient {

    override suspend fun getPosts(
        page: Int,
        limit: Int,
        query: String?,
    ): Result<PostsPageResponseDto> = authorizedRequestExecutor.execute {
        httpClient.get(ApiRoutes.Posts) {
            url {
                parameters.append("page", page.toString())
                parameters.append("limit", limit.toString())
                query?.takeIf { it.isNotBlank() }?.let { parameters.append("query", it) }
            }
        }.body<PostsPageResponseDto>()
    }

    override suspend fun getPost(id: String): Result<PostResponseDto> =
        authorizedRequestExecutor.execute {
            httpClient.get(ApiRoutes.postById(id)).body<PostResponseDto>()
        }

    override suspend fun vote(
        id: String,
        request: VoteRequestDto,
    ): Result<PostVoteResponseDto> = authorizedRequestExecutor.execute {
        httpClient.post(ApiRoutes.postVote(id)) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<PostVoteResponseDto>()
    }
}
