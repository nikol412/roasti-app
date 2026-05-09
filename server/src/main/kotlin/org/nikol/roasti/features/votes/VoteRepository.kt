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

data class VoteInfo(val rating: Int, val userVote: String?)

const val VOTE_TYPE_UP = "up"
const val VOTE_TYPE_DOWN = "down"

interface VoteRepository {
    suspend fun upsert(userId: UserId, targetId: String, targetType: String, voteType: String)
    suspend fun delete(userId: UserId, targetId: String, targetType: String)
    suspend fun getInfo(userId: UserId?, targetId: String, targetType: String): VoteInfo
    suspend fun getInfoBatch(userId: UserId?, targetIds: List<String>, targetType: String): Map<String, VoteInfo>
}

class VoteRepositoryImpl : VoteRepository {

    override suspend fun upsert(userId: UserId, targetId: String, targetType: String, voteType: String): Unit =
        withContext(Dispatchers.IO) {
            transaction {
                VoteTable.upsert {
                    it[VoteTable.userId] = userId.value
                    it[VoteTable.targetId] = targetId
                    it[VoteTable.targetType] = targetType
                    it[VoteTable.voteType] = voteType
                    it[VoteTable.createdAt] = Clock.System.now()
                }
            }
        }

    override suspend fun delete(userId: UserId, targetId: String, targetType: String): Unit =
        withContext(Dispatchers.IO) {
            transaction {
                VoteTable.deleteWhere {
                    (VoteTable.userId eq userId.value) and
                        (VoteTable.targetId eq targetId) and
                        (VoteTable.targetType eq targetType)
                }
            }
        }

    override suspend fun getInfo(userId: UserId?, targetId: String, targetType: String): VoteInfo =
        withContext(Dispatchers.IO) {
            transaction {
                val rows = VoteTable.selectAll()
                    .where { (VoteTable.targetId eq targetId) and (VoteTable.targetType eq targetType) }

                val rating = rows.sumOf { row ->
                    when (row[VoteTable.voteType]) {
                        VOTE_TYPE_UP -> 1
                        VOTE_TYPE_DOWN -> -1
                        else -> 0
                    }
                }
                val userVote = userId?.let { uid ->
                    rows.find { it[VoteTable.userId] == uid.value }?.get(VoteTable.voteType)
                }
                VoteInfo(rating, userVote)
            }
        }

    override suspend fun getInfoBatch(userId: UserId?, targetIds: List<String>, targetType: String): Map<String, VoteInfo> =
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
                            VOTE_TYPE_UP -> 1
                            VOTE_TYPE_DOWN -> -1
                            else -> 0
                        }
                    }
                    val userVote = userId?.let { uid ->
                        group.find { it[VoteTable.userId] == uid.value }?.get(VoteTable.voteType)
                    }
                    VoteInfo(rating, userVote)
                }
            }
        }
}
