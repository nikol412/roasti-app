package org.nikol.roasti.features.votes

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp

object VoteTable : Table("votes") {
    val userId = varchar("user_id", 128)
    val targetId = varchar("target_id", 128)
    val targetType = varchar("target_type", 50)
    val voteType = varchar("vote_type", 10)
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(userId, targetId, targetType)
}
