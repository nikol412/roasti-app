package org.nikol.roasti.features.comments

import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.uuid.ExperimentalUuidApi

enum class CommentTargetType { POST, RECIPE }

object CommentTable : UuidTable("comments") {
    val targetId = varchar("target_id", 128)
    val targetType = enumerationByName<CommentTargetType>("target_type", 50)
    val authorId = varchar("author_id", 128)
    val text = text("text")
    @OptIn(ExperimentalUuidApi::class)
    val parentId = uuid("parent_id").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val deletedAt = timestamp("deleted_at").nullable()
}
