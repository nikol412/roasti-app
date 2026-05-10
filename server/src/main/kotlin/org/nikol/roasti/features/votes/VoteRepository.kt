package org.nikol.roasti.features.votes

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert
import org.nikol.roasti.features.users.UserId
import kotlin.time.Clock

enum class VoteDirection { UP, DOWN, NONE }

enum class VoteTargetType { POST }

data class VoteInfo(val rating: Int, val userVote: VoteDirection)

interface VoteRepository {
    suspend fun upsert(userId: UserId, targetId: String, targetType: VoteTargetType, direction: VoteDirection)
    suspend fun delete(userId: UserId, targetId: String, targetType: VoteTargetType)
    suspend fun getInfo(userId: UserId?, targetId: String, targetType: VoteTargetType): VoteInfo
    suspend fun getInfoBatch(userId: UserId?, targetIds: List<String>, targetType: VoteTargetType): Map<String, VoteInfo>
}

class VoteRepositoryImpl : VoteRepository {

    override suspend fun upsert(userId: UserId, targetId: String, targetType: VoteTargetType, direction: VoteDirection): Unit =
        withContext(Dispatchers.IO) {
            transaction {
                VoteTable.upsert {
                    it[VoteTable.userId] = userId.value
                    it[VoteTable.targetId] = targetId
                    it[VoteTable.targetType] = targetType
                    it[VoteTable.voteType] = direction
                    it[VoteTable.createdAt] = Clock.System.now()
                }
            }
        }

    override suspend fun delete(userId: UserId, targetId: String, targetType: VoteTargetType): Unit =
        withContext(Dispatchers.IO) {
            transaction {
                VoteTable.deleteWhere {
                    (VoteTable.userId eq userId.value) and
                        (VoteTable.targetId eq targetId) and
                        (VoteTable.targetType eq targetType)
                }
            }
        }

    override suspend fun getInfo(userId: UserId?, targetId: String, targetType: VoteTargetType): VoteInfo =
        getInfoBatch(userId, listOf(targetId), targetType)[targetId] ?: VoteInfo(0, VoteDirection.NONE)

    override suspend fun getInfoBatch(userId: UserId?, targetIds: List<String>, targetType: VoteTargetType): Map<String, VoteInfo> =
        withContext(Dispatchers.IO) {
            if (targetIds.isEmpty()) return@withContext emptyMap()
            transaction {
                val rows = VoteTable.selectAll()
                    .where { (VoteTable.targetId inList targetIds) and (VoteTable.targetType eq targetType) }

                val grouped = rows.groupBy { it[VoteTable.targetId] }
                targetIds.associateWith { id ->
                    val group = grouped[id] ?: emptyList()
                    val rating = group.sumOf { row ->
                        when (row[VoteTable.voteType]) {
                            VoteDirection.UP -> 1
                            VoteDirection.DOWN -> -1
                            VoteDirection.NONE -> 0
                        }
                    }
                    val userVote = userId?.let { uid ->
                        group.find { it[VoteTable.userId] == uid.value }?.get(VoteTable.voteType)
                    } ?: VoteDirection.NONE
                    VoteInfo(rating, userVote)
                }
            }
        }
}
