package org.nikol.roasti.features.comments

import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.datetime.timestamp

object CommentTable : UuidTable("comments") {
    val targetId = varchar("target_id", 128)
    val targetType = varchar("target_type", 50)
    val authorId = varchar("author_id", 128)
    val text = text("text")
    val parentId = uuid("parent_id").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val deletedAt = timestamp("deleted_at").nullable()
}
