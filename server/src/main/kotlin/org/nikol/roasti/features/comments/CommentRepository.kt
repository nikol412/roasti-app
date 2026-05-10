package org.nikol.roasti.features.comments

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.isNotNull
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.nikol.roasti.features.users.UserTable
import org.nikol.roasti.features.users.UserId
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class CreateCommentInput(
    val targetId: String,
    val targetType: String,
    val authorId: UserId,
    val text: String,
    val parentId: CommentId?,
)

interface CommentRepository {
    suspend fun create(input: CreateCommentInput): Comment
    suspend fun findById(id: CommentId): Comment?
    suspend fun getAuthorId(id: CommentId): UserId?
    suspend fun update(id: CommentId, text: String)
    suspend fun softDelete(id: CommentId)
    suspend fun listForTarget(targetId: String, targetType: String, page: Int, limit: Int): Pair<List<CommentThread>, Int>
    suspend fun existsInTarget(commentId: CommentId, targetId: String): Boolean
    suspend fun countForTarget(targetId: String, targetType: String): Int
    suspend fun countForTargetBatch(targetIds: List<String>, targetType: String): Map<String, Int>
}

@OptIn(ExperimentalUuidApi::class)
class CommentRepositoryImpl : CommentRepository {

    override suspend fun create(input: CreateCommentInput): Comment = withContext(Dispatchers.IO) {
        val id = Uuid.random()
        val now = Clock.System.now()
        transaction {
            CommentTable.insert {
                it[CommentTable.id] = org.jetbrains.exposed.v1.core.dao.id.EntityID(id, CommentTable)
                it[targetId] = input.targetId
                it[targetType] = input.targetType
                it[authorId] = input.authorId.value
                it[text] = input.text
                it[parentId] = input.parentId?.value
                it[createdAt] = now
                it[updatedAt] = now
            }
        }
        findById(CommentId(id))!!
    }

    override suspend fun findById(id: CommentId): Comment? = withContext(Dispatchers.IO) {
        transaction {
            CommentTable.innerJoin(UserTable)
                .selectAll()
                .where { (CommentTable.id eq id.value) and CommentTable.deletedAt.isNull() }
                .singleOrNull()
                ?.toComment()
        }
    }

    override suspend fun getAuthorId(id: CommentId): UserId? = withContext(Dispatchers.IO) {
        transaction {
            CommentTable.selectAll()
                .where { (CommentTable.id eq id.value) and CommentTable.deletedAt.isNull() }
                .singleOrNull()
                ?.let { UserId(it[CommentTable.authorId]) }
        }
    }

    override suspend fun update(id: CommentId, text: String): Unit = withContext(Dispatchers.IO) {
        transaction {
            CommentTable.update({ CommentTable.id eq id.value }) {
                it[CommentTable.text] = text
                it[updatedAt] = Clock.System.now()
            }
        }
    }

    override suspend fun softDelete(id: CommentId): Unit = withContext(Dispatchers.IO) {
        transaction {
            CommentTable.update({ CommentTable.id eq id.value }) {
                it[deletedAt] = Clock.System.now()
            }
        }
    }

    override suspend fun listForTarget(
        targetId: String,
        targetType: String,
        page: Int,
        limit: Int,
    ): Pair<List<CommentThread>, Int> = withContext(Dispatchers.IO) {
        transaction {
            val total = CommentTable.selectAll()
                .where {
                    (CommentTable.targetId eq targetId) and
                        (CommentTable.targetType eq targetType) and
                        CommentTable.parentId.isNull()
                }
                .count()
                .toInt()

            if (total == 0) return@transaction emptyList<CommentThread>() to 0

            val offset = (page - 1) * limit

            val roots = CommentTable.innerJoin(UserTable)
                .selectAll()
                .where {
                    (CommentTable.targetId eq targetId) and
                        (CommentTable.targetType eq targetType) and
                        CommentTable.parentId.isNull()
                }
                .orderBy(CommentTable.createdAt)
                .limit(limit)
                .offset(offset.toLong())
                .map { it.toCommentThread() }

            if (roots.isEmpty()) return@transaction emptyList<CommentThread>() to total

            val rootIdxMap = roots.mapIndexed { i, c -> c.root.id.value to i }.toMap()

            // Load all non-root comments for target, build tree in Kotlin
            val allReplies = CommentTable.innerJoin(UserTable)
                .selectAll()
                .where {
                    (CommentTable.targetId eq targetId) and
                        (CommentTable.targetType eq targetType) and
                        CommentTable.parentId.isNotNull()
                }
                .orderBy(CommentTable.createdAt)
                .map { it.toComment() }

            // Map each reply to its root ancestor
            val commentToRoot = mutableMapOf<Uuid, Int>()
            val replyGroups = Array<MutableList<Comment>>(roots.size) { mutableListOf() }

            for (reply in allReplies) {
                val parentId = reply.parentId?.value ?: continue
                val rootIdx = rootIdxMap[parentId] ?: commentToRoot[parentId] ?: continue
                commentToRoot[reply.id.value] = rootIdx
                replyGroups[rootIdx].add(reply)
            }

            val threads = roots.mapIndexed { i, thread -> thread.copy(replies = replyGroups[i]) }
            threads to total
        }
    }

    override suspend fun existsInTarget(commentId: CommentId, targetId: String): Boolean = withContext(Dispatchers.IO) {
        transaction {
            CommentTable.selectAll()
                .where {
                    (CommentTable.id eq commentId.value) and
                        (CommentTable.targetId eq targetId) and
                        CommentTable.deletedAt.isNull()
                }
                .count() > 0
        }
    }

    override suspend fun countForTarget(targetId: String, targetType: String): Int =
        countForTargetBatch(listOf(targetId), targetType)[targetId] ?: 0

    override suspend fun countForTargetBatch(targetIds: List<String>, targetType: String): Map<String, Int> =
        withContext(Dispatchers.IO) {
            if (targetIds.isEmpty()) return@withContext emptyMap()
            transaction {
                CommentTable.selectAll()
                    .where {
                        (CommentTable.targetId inList targetIds) and
                            (CommentTable.targetType eq targetType) and
                            CommentTable.deletedAt.isNull()
                    }
                    .groupBy { it[CommentTable.targetId] }
                    .mapValues { (_, rows) -> rows.size }
                    .let { counts -> targetIds.associateWith { counts[it] ?: 0 } }
            }
        }
}

@OptIn(ExperimentalUuidApi::class)
private fun org.jetbrains.exposed.v1.core.ResultRow.toComment(): Comment {
    val deletedAt = this[CommentTable.deletedAt]
    return if (deletedAt != null) {
        Comment(
            id = CommentId(this[CommentTable.id].value),
            isDeleted = true,
            author = null,
            text = "",
            parentId = this[CommentTable.parentId]?.let { CommentId(it) },
            createdAt = this[CommentTable.createdAt],
            updatedAt = this[CommentTable.updatedAt],
        )
    } else {
        Comment(
            id = CommentId(this[CommentTable.id].value),
            isDeleted = false,
            author = CommentAuthor(
                id = UserId(this[UserTable.id]),
                username = this[UserTable.username],
                name = this[UserTable.name],
                avatarId = this[UserTable.avatarId],
            ),
            text = this[CommentTable.text],
            parentId = this[CommentTable.parentId]?.let { CommentId(it) },
            createdAt = this[CommentTable.createdAt],
            updatedAt = this[CommentTable.updatedAt],
        )
    }
}

private fun org.jetbrains.exposed.v1.core.ResultRow.toCommentThread(): CommentThread =
    CommentThread(root = toComment().copy(parentId = null), replies = emptyList())

