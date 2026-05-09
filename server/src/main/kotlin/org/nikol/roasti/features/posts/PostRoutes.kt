package org.nikol.roasti.features.posts

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import org.nikol.roasti.FIREBASE_AUTH
import org.nikol.roasti.FirebasePrincipal
import org.nikol.roasti.feature.comment.data.remote.model.request.CreateCommentRequestDto
import org.nikol.roasti.feature.comment.data.remote.model.response.CommentAuthorDto
import org.nikol.roasti.feature.comment.data.remote.model.response.CommentResponseDto
import org.nikol.roasti.feature.comment.data.remote.model.response.CommentThreadResponseDto
import org.nikol.roasti.feature.comment.data.remote.model.response.CommentsPaginationResponseDto
import org.nikol.roasti.feature.comment.data.remote.model.response.CommentsPageResponseDto
import org.nikol.roasti.feature.post.data.remote.model.VoteDirectionDto
import org.nikol.roasti.feature.post.data.remote.model.request.CreatePostRequestDto
import org.nikol.roasti.feature.post.data.remote.model.request.UpdatePostRequestDto
import org.nikol.roasti.feature.post.data.remote.model.request.VoteRequestDto
import org.nikol.roasti.feature.post.data.remote.model.response.PostAuthorDto
import org.nikol.roasti.feature.post.data.remote.model.response.PostResponseDto
import org.nikol.roasti.feature.post.data.remote.model.response.PostVoteResponseDto
import org.nikol.roasti.feature.post.data.remote.model.response.PostsPaginationResponseDto
import org.nikol.roasti.feature.post.data.remote.model.response.PostsPageResponseDto
import org.nikol.roasti.features.comments.Comment
import org.nikol.roasti.features.comments.CommentAuthor
import org.nikol.roasti.features.comments.CommentId
import org.nikol.roasti.features.comments.CommentService
import org.nikol.roasti.features.comments.CommentThread
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun Route.postRoutes() {
    val postService by inject<PostService>()
    val commentService by inject<CommentService>()

    route("/posts") {
        get {
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
            val authorId = call.request.queryParameters["author_id"]
                ?.let { org.nikol.roasti.features.users.UserId(it) }
            val userId = call.principal<FirebasePrincipal>()?.uid
            val page2 = postService.list(page, limit, authorId)
            call.respond(page2.toDto(userId))
        }

        get("/{id}") {
            val id = call.parameters["id"]?.let { PostId(Uuid.parse(it)) }
                ?: return@get call.respond(HttpStatusCode.BadRequest)
            val userId = call.principal<FirebasePrincipal>()?.uid
            val post = postService.getById(id, userId)
            call.respond(post.toDto())
        }

        get("/{id}/comments") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
            val result = commentService.list(id, POST_TARGET_TYPE, page, limit)
            call.respond(
                CommentsPageResponseDto(
                    items = result.items.map { it.toDto() },
                    pagination = CommentsPaginationResponseDto(
                        currentPage = result.currentPage,
                        itemsCount = result.itemsCount,
                        lastPage = result.lastPage,
                        nextPage = result.nextPage,
                    ),
                )
            )
        }

        authenticate(FIREBASE_AUTH) {
            post {
                val userId = call.principal<FirebasePrincipal>()!!.uid
                val body = call.receive<CreatePostRequestDto>()
                val post = postService.create(
                    userId,
                    CreatePostInput(
                        title = body.title,
                        text = body.text,
                        images = body.images,
                        recipeId = body.recipeId?.let { Uuid.parse(it) },
                    ),
                )
                call.respond(HttpStatusCode.Created, post.toDto())
            }

            put("/{id}") {
                val id = call.parameters["id"]?.let { PostId(Uuid.parse(it)) }
                    ?: return@put call.respond(HttpStatusCode.BadRequest)
                val userId = call.principal<FirebasePrincipal>()!!.uid
                val body = call.receive<UpdatePostRequestDto>()
                val post = postService.update(
                    userId, id,
                    UpdatePostInput(
                        title = body.title,
                        text = body.text,
                        images = body.images,
                        recipeId = body.recipeId?.let { Uuid.parse(it) },
                    ),
                )
                call.respond(post.toDto())
            }

            delete("/{id}") {
                val id = call.parameters["id"]?.let { PostId(Uuid.parse(it)) }
                    ?: return@delete call.respond(HttpStatusCode.BadRequest)
                val userId = call.principal<FirebasePrincipal>()!!.uid
                postService.delete(userId, id)
                call.respond(HttpStatusCode.NoContent)
            }

            put("/{id}/vote") {
                val id = call.parameters["id"]?.let { PostId(Uuid.parse(it)) }
                    ?: return@put call.respond(HttpStatusCode.BadRequest)
                val userId = call.principal<FirebasePrincipal>()!!.uid
                val body = call.receive<VoteRequestDto>()
                val direction = body.type.toDomain()
                val result = postService.vote(userId, id, direction)
                call.respond(
                    PostVoteResponseDto(
                        rating = result.rating,
                        userVote = result.userVote.toDto(),
                    )
                )
            }

            post("/{id}/comments") {
                val id = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                val userId = call.principal<FirebasePrincipal>()!!.uid
                val body = call.receive<CreateCommentRequestDto>()
                val parentId = body.parentId?.let { CommentId(Uuid.parse(it)) }
                val comment = commentService.create(userId, id, POST_TARGET_TYPE, body.text, parentId)
                call.respond(HttpStatusCode.Created, comment.toDto())
            }
        }
    }
}

private fun VoteDirectionDto.toDomain() = when (this) {
    VoteDirectionDto.UP -> VoteDirection.UP
    VoteDirectionDto.DOWN -> VoteDirection.DOWN
    VoteDirectionDto.NONE -> VoteDirection.NONE
}

private fun VoteDirection.toDto() = when (this) {
    VoteDirection.UP -> VoteDirectionDto.UP
    VoteDirection.DOWN -> VoteDirectionDto.DOWN
    VoteDirection.NONE -> VoteDirectionDto.NONE
}

@OptIn(ExperimentalUuidApi::class)
private fun Post.toDto() = PostResponseDto(
    id = id.value.toString(),
    author = PostAuthorDto(
        id = author.id.value,
        username = author.username,
        name = author.name,
        avatarId = author.avatarId,
    ),
    title = title,
    text = text ?: "",
    images = images,
    recipeId = recipeId?.toString(),
    rating = rating,
    userVote = userVote.toDto(),
    commentsCount = commentsCount,
    createdAt = kotlinx.datetime.Instant.fromEpochMilliseconds(createdAt.toEpochMilliseconds()),
    updatedAt = kotlinx.datetime.Instant.fromEpochMilliseconds(updatedAt.toEpochMilliseconds()),
)

@OptIn(ExperimentalUuidApi::class)
private fun PostsPage.toDto(userId: org.nikol.roasti.features.users.UserId?) = PostsPageResponseDto(
    items = items.map { it.toDto() },
    pagination = PostsPaginationResponseDto(
        currentPage = currentPage,
        itemsCount = itemsCount,
        lastPage = lastPage,
        nextPage = nextPage,
    ),
)

private fun CommentAuthor.toDto() = CommentAuthorDto(
    id = id.value,
    username = username,
    name = name,
    avatarId = avatarId,
)

@OptIn(ExperimentalUuidApi::class)
private fun Comment.toDto() = CommentResponseDto(
    id = id.value.toString(),
    isDeleted = isDeleted,
    author = author?.toDto(),
    text = text,
    parentId = parentId?.value?.toString(),
    createdAt = kotlinx.datetime.Instant.fromEpochMilliseconds(createdAt.toEpochMilliseconds()),
    updatedAt = kotlinx.datetime.Instant.fromEpochMilliseconds(updatedAt.toEpochMilliseconds()),
)

@OptIn(ExperimentalUuidApi::class)
private fun CommentThread.toDto() = CommentThreadResponseDto(
    id = id.value.toString(),
    isDeleted = isDeleted,
    author = author?.toDto(),
    text = text,
    parentId = parentId?.value?.toString(),
    replies = replies.map { it.toDto() },
    createdAt = kotlinx.datetime.Instant.fromEpochMilliseconds(createdAt.toEpochMilliseconds()),
    updatedAt = kotlinx.datetime.Instant.fromEpochMilliseconds(updatedAt.toEpochMilliseconds()),
)
