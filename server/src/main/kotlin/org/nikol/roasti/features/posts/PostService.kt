package org.nikol.roasti.features.posts

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.nikol.roasti.features.comments.CommentRepository
import org.nikol.roasti.features.comments.CommentTargetType
import org.nikol.roasti.features.common.Page
import org.nikol.roasti.features.users.UserId
import org.nikol.roasti.features.votes.VoteDirection
import org.nikol.roasti.features.votes.VoteService
import org.nikol.roasti.features.votes.VoteTargetType
import kotlin.uuid.ExperimentalUuidApi

data class PostVoteResult(val rating: Int, val userVote: VoteDirection)

interface PostService {
    suspend fun getById(id: PostId, userId: UserId?): Either<PostErrorCode, Post>
    suspend fun list(page: Int, limit: Int, authorId: UserId?, userId: UserId?): Page<Post>
    suspend fun create(userId: UserId, input: CreatePostInput): Post
    suspend fun update(userId: UserId, id: PostId, input: UpdatePostInput): Either<PostErrorCode, Post>
    suspend fun delete(userId: UserId, id: PostId): Either<PostErrorCode, Unit>
    suspend fun vote(userId: UserId, id: PostId, direction: VoteDirection): Either<PostErrorCode, PostVoteResult>
}

@OptIn(ExperimentalUuidApi::class)
class PostServiceImpl(
    private val repo: PostRepository,
    private val voteService: VoteService,
    private val commentRepo: CommentRepository,
) : PostService {

    override suspend fun getById(id: PostId, userId: UserId?): Either<PostErrorCode, Post> {
        val row = repo.findById(id) ?: return PostErrorCode.NOT_FOUND.left()
        return row.enrich(userId).right()
    }

    override suspend fun list(page: Int, limit: Int, authorId: UserId?, userId: UserId?): Page<Post> {
        val (rows, total) = repo.list(page, limit, authorId)
        val postIds = rows.map { it.id.value.toString() }
        val voteInfos = voteService.getInfoBatch(userId, postIds, VoteTargetType.POST)
        val commentCounts = commentRepo.countForTargetBatch(postIds, CommentTargetType.POST)

        val posts = rows.map { row ->
            val id = row.id.value.toString()
            val voteInfo = voteInfos.getValue(id)
            row.toPost(
                rating = voteInfo.rating,
                userVote = voteInfo.userVote,
                commentsCount = commentCounts.getValue(id),
            )
        }

        return Page.of(posts, page, total, limit)
    }

    override suspend fun create(userId: UserId, input: CreatePostInput): Post {
        val row = repo.create(userId, input)
        return row.enrich(userId)
    }

    override suspend fun update(userId: UserId, id: PostId, input: UpdatePostInput): Either<PostErrorCode, Post> {
        val existing = repo.findById(id) ?: return PostErrorCode.NOT_FOUND.left()
        if (existing.author.id != userId) return PostErrorCode.FORBIDDEN.left()
        val row = repo.update(id, input) ?: return PostErrorCode.NOT_FOUND.left()
        return row.enrich(userId).right()
    }

    override suspend fun delete(userId: UserId, id: PostId): Either<PostErrorCode, Unit> {
        val existing = repo.findById(id) ?: return PostErrorCode.NOT_FOUND.left()
        if (existing.author.id != userId) return PostErrorCode.FORBIDDEN.left()
        repo.delete(id)
        return Unit.right()
    }

    override suspend fun vote(userId: UserId, id: PostId, direction: VoteDirection): Either<PostErrorCode, PostVoteResult> {
        repo.findById(id) ?: return PostErrorCode.NOT_FOUND.left()
        val targetId = id.value.toString()
        val voteInfo = when (direction) {
            VoteDirection.UP, VoteDirection.DOWN -> voteService.put(userId, targetId, VoteTargetType.POST, direction)
            VoteDirection.NONE -> voteService.remove(userId, targetId, VoteTargetType.POST)
        }
        return PostVoteResult(voteInfo.rating, voteInfo.userVote).right()
    }

    private suspend fun PostRow.enrich(userId: UserId?): Post {
        val targetId = id.value.toString()
        val voteInfo = voteService.getInfo(userId, targetId, VoteTargetType.POST)
        val commentsCount = commentRepo.countForTarget(targetId, CommentTargetType.POST)
        return toPost(voteInfo.rating, voteInfo.userVote, commentsCount)
    }
}

@OptIn(ExperimentalUuidApi::class)
internal fun PostRow.toPost(rating: Int, userVote: VoteDirection, commentsCount: Int) = Post(
    id = id,
    author = author,
    title = title,
    text = text,
    images = images,
    recipeId = recipeId,
    rating = rating,
    userVote = userVote,
    commentsCount = commentsCount,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
