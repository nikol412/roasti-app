package org.nikol.roasti.features.comments

import org.nikol.roasti.features.common.Page
import org.nikol.roasti.features.users.UserId

private const val TEXT_MAX_LENGTH = 1000

interface CommentService {
    suspend fun create(userId: UserId, targetId: String, targetType: CommentTargetType, text: String, parentId: CommentId?): Comment
    suspend fun update(userId: UserId, commentId: CommentId, text: String): Comment
    suspend fun delete(userId: UserId, commentId: CommentId)
    suspend fun list(targetId: String, targetType: CommentTargetType, page: Int, limit: Int): Page<CommentThread>
}

class CommentServiceImpl(private val repo: CommentRepository) : CommentService {

    override suspend fun create(
        userId: UserId,
        targetId: String,
        targetType: CommentTargetType,
        text: String,
        parentId: CommentId?,
    ): Comment {
        val normalized = text.trim()
        require(normalized.isNotEmpty()) { "comment text must not be empty" }
        require(normalized.length <= TEXT_MAX_LENGTH) { "comment text must be at most $TEXT_MAX_LENGTH characters" }

        if (parentId != null) {
            require(repo.existsInTarget(parentId, targetId)) { "parent comment not found" }
        }

        return repo.create(
            CreateCommentInput(
                targetId = targetId,
                targetType = targetType,
                authorId = userId,
                text = normalized,
                parentId = parentId,
            )
        )
    }

    override suspend fun update(userId: UserId, commentId: CommentId, text: String): Comment {
        val authorId = repo.getAuthorId(commentId) ?: throw CommentNotFoundException
        if (authorId != userId) throw CommentForbiddenException
        val normalized = text.trim()
        require(normalized.isNotEmpty()) { "comment text must not be empty" }
        require(normalized.length <= TEXT_MAX_LENGTH) { "comment text must be at most $TEXT_MAX_LENGTH characters" }
        repo.update(commentId, normalized)
        return repo.findById(commentId) ?: throw CommentNotFoundException
    }

    override suspend fun delete(userId: UserId, commentId: CommentId) {
        val authorId = repo.getAuthorId(commentId) ?: throw CommentNotFoundException
        if (authorId != userId) throw CommentForbiddenException
        repo.softDelete(commentId)
    }

    override suspend fun list(targetId: String, targetType: CommentTargetType, page: Int, limit: Int): Page<CommentThread> {
        val (items, total) = repo.listForTarget(targetId, targetType, page, limit)
        return Page.of(items, page, total, limit)
    }
}

val CommentNotFoundException = NoSuchElementException("comment not found")
val CommentForbiddenException = IllegalStateException("forbidden")
