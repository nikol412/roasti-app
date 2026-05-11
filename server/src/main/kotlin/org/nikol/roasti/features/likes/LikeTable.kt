package org.nikol.roasti.features.likes

import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.datetime.timestamp
import org.nikol.roasti.features.users.UserTable
import kotlin.uuid.ExperimentalUuidApi

enum class LikeTargetType { RECIPE }

// TODO: consider deleting the id and using a combination of fields
//  as the key - user_id, target_id, target_type
@OptIn(ExperimentalUuidApi::class)
object LikeTable : UuidTable("likes") {
    val userId = reference("user_id", UserTable)
    val targetId = uuid("target_id")
    val targetType = enumerationByName<LikeTargetType>("target_type", 50)
    val createdAt = timestamp("created_at")

    init {
        uniqueIndex(userId, targetId, targetType)
    }
}
