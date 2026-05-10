package org.nikol.roasti.features.likes

import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.datetime.timestamp

enum class LikeTargetType { RECIPE }

object LikeTable : UuidTable("likes") {
    val userId = varchar("user_id", 128)
    val targetId = varchar("target_id", 128)
    val targetType = enumerationByName<LikeTargetType>("target_type", 50)
    val createdAt = timestamp("created_at")

    init {
        uniqueIndex(userId, targetId, targetType)
    }
}
