package org.nikol.roasti.features.posts

import org.nikol.roasti.features.comments.CommentRepository
import org.nikol.roasti.features.users.UserId
import org.nikol.roasti.features.votes.VOTE_TYPE_DOWN
import org.nikol.roasti.features.votes.VOTE_TYPE_UP
import org.nikol.roasti.features.votes.VoteService
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

const val POST_TARGET_TYPE = "post"

data class PostVoteResult(val rating: Int, val userVote: VoteDirection)

interface PostService {
    suspend fun getById(id: PostId, userId: UserId?): Post
    suspend fun list(page: Int, limit: Int, authorId: UserId?): PostsPage
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

    override suspend fun list(page: Int, limit: Int, authorId: UserId?): PostsPage {
        val (rows, total) = repo.list(page, limit, authorId)
        val postIds = rows.map { it.id.value.toString() }
        val voteInfos = voteService.getInfoBatch(null, postIds, POST_TARGET_TYPE)
        val commentCounts = postIds.associateWith { commentRepo.countForTarget(it, POST_TARGET_TYPE) }

        val posts = rows.map { row ->
            val id = row.id.value.toString()
            val voteInfo = voteInfos[id]
            row.toPost(
                rating = voteInfo?.rating ?: 0,
                userVote = VoteDirection.NONE,
                commentsCount = commentCounts[id] ?: 0,
            )
        }

        val lastPage = if (total == 0) 1 else (total + limit - 1) / limit
        return PostsPage(
            items = posts,
            currentPage = page,
            itemsCount = total,
            lastPage = lastPage,
            nextPage = if (page < lastPage) page + 1 else page,
        )
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
            VoteDirection.UP -> voteService.put(userId, targetId, POST_TARGET_TYPE, VOTE_TYPE_UP)
            VoteDirection.DOWN -> voteService.put(userId, targetId, POST_TARGET_TYPE, VOTE_TYPE_DOWN)
            VoteDirection.NONE -> voteService.remove(userId, targetId, POST_TARGET_TYPE)
        }
        val userVote = when (voteInfo.userVote) {
            VOTE_TYPE_UP -> VoteDirection.UP
            VOTE_TYPE_DOWN -> VoteDirection.DOWN
            else -> VoteDirection.NONE
        }
        return PostVoteResult(voteInfo.rating, userVote)
    }

    private suspend fun PostRow.enrich(userId: UserId?): Post {
        val targetId = id.value.toString()
        val voteInfo = voteService.getInfo(userId, targetId, POST_TARGET_TYPE)
        val commentsCount = commentRepo.countForTarget(targetId, POST_TARGET_TYPE)
        val userVote = when (voteInfo.userVote) {
            VOTE_TYPE_UP -> VoteDirection.UP
            VOTE_TYPE_DOWN -> VoteDirection.DOWN
            else -> VoteDirection.NONE
        }
        return toPost(voteInfo.rating, userVote, commentsCount)
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
