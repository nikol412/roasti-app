package org.nikol.roasti.features.posts

import org.nikol.roasti.features.comments.CommentRepository
import org.nikol.roasti.features.comments.CommentTargetType
import org.nikol.roasti.features.common.Page
import org.nikol.roasti.features.users.UserId
import org.nikol.roasti.features.votes.VoteDirection
import org.nikol.roasti.features.votes.VoteService
import org.nikol.roasti.features.votes.VoteTargetType
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


data class PostVoteResult(val rating: Int, val userVote: VoteDirection)

interface PostService {
    suspend fun getById(id: PostId, userId: UserId?): Post
    suspend fun list(page: Int, limit: Int, authorId: UserId?, userId: UserId?): Page<Post>
    suspend fun create(userId: UserId, input: CreatePostInput): Post
    suspend fun update(userId: UserId, id: PostId, input: UpdatePostInput): Post
    suspend fun delete(userId: UserId, id: PostId)
    suspend fun vote(userId: UserId, id: PostId, direction: VoteDirection): PostVoteResult
}

@OptIn(ExperimentalUuidApi::class)
class PostServiceImpl(
    private val repo: PostRepository,
    private val voteService: VoteService,
    private val commentRepo: CommentRepository,
) : PostService {

    override suspend fun getById(id: PostId, userId: UserId?): Post {
        val row = repo.findById(id) ?: throw PostNotFoundException
        return row.enrich(userId)
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

    override suspend fun update(userId: UserId, id: PostId, input: UpdatePostInput): Post {
        val existing = repo.findById(id) ?: throw PostNotFoundException
        if (existing.author.id != userId) throw PostForbiddenException
        val row = repo.update(id, input) ?: throw PostNotFoundException
        return row.enrich(userId)
    }

    override suspend fun delete(userId: UserId, id: PostId) {
        val existing = repo.findById(id) ?: throw PostNotFoundException
        if (existing.author.id != userId) throw PostForbiddenException
        repo.delete(id)
    }

    override suspend fun vote(userId: UserId, id: PostId, direction: VoteDirection): PostVoteResult {
        repo.findById(id) ?: throw PostNotFoundException
        val targetId = id.value.toString()
        val voteInfo = when (direction) {
            VoteDirection.UP, VoteDirection.DOWN -> voteService.put(userId, targetId, VoteTargetType.POST, direction)
            VoteDirection.NONE -> voteService.remove(userId, targetId, VoteTargetType.POST)
        }
        return PostVoteResult(voteInfo.rating, voteInfo.userVote)
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

val PostNotFoundException = NoSuchElementException("post not found")
val PostForbiddenException = IllegalStateException("forbidden")
