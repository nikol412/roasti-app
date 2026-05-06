package org.nikol.roasti.feature.comment.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.nikol.roasti.core.network.ApiRoutes
import org.nikol.roasti.core.network.AuthorizedRequestExecutor
import org.nikol.roasti.feature.comment.data.remote.model.request.CreateCommentRequestDto
import org.nikol.roasti.feature.comment.data.remote.model.response.CommentResponseDto
import org.nikol.roasti.feature.comment.data.remote.model.response.CommentsPageResponseDto

interface CommentsApiClient {
    suspend fun listComments(
        postId: String,
        page: Int,
        limit: Int,
    ): Result<CommentsPageResponseDto>

    suspend fun createComment(
        postId: String,
        request: CreateCommentRequestDto,
    ): Result<CommentResponseDto>
}

class CommentsApiClientImpl(
    private val httpClient: HttpClient,
    private val authorizedRequestExecutor: AuthorizedRequestExecutor,
) : CommentsApiClient {

    override suspend fun listComments(
        postId: String,
        page: Int,
        limit: Int,
    ): Result<CommentsPageResponseDto> = authorizedRequestExecutor.execute {
        httpClient.get(ApiRoutes.postComments(postId)) {
            url {
                parameters.append("page", page.toString())
                parameters.append("limit", limit.toString())
            }
        }.body<CommentsPageResponseDto>()
    }

    override suspend fun createComment(
        postId: String,
        request: CreateCommentRequestDto,
    ): Result<CommentResponseDto> = authorizedRequestExecutor.execute {
        httpClient.post(ApiRoutes.postComments(postId)) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<CommentResponseDto>()
    }
}
