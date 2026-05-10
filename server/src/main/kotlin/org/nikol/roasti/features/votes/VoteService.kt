package org.nikol.roasti.features.votes

import org.nikol.roasti.features.users.UserId

interface VoteService {
    suspend fun put(userId: UserId, targetId: String, targetType: VoteTargetType, direction: VoteDirection): VoteInfo
    suspend fun remove(userId: UserId, targetId: String, targetType: VoteTargetType): VoteInfo
    suspend fun getInfo(userId: UserId?, targetId: String, targetType: VoteTargetType): VoteInfo
    suspend fun getInfoBatch(userId: UserId?, targetIds: List<String>, targetType: VoteTargetType): Map<String, VoteInfo>
}

class VoteServiceImpl(private val repo: VoteRepository) : VoteService {

    override suspend fun put(userId: UserId, targetId: String, targetType: VoteTargetType, direction: VoteDirection): VoteInfo {
        repo.upsert(userId, targetId, targetType, direction)
        return repo.getInfo(userId, targetId, targetType)
    }

    override suspend fun remove(userId: UserId, targetId: String, targetType: VoteTargetType): VoteInfo {
        repo.delete(userId, targetId, targetType)
        return repo.getInfo(userId, targetId, targetType)
    }

    override suspend fun getInfo(userId: UserId?, targetId: String, targetType: VoteTargetType): VoteInfo =
        repo.getInfo(userId, targetId, targetType)

    override suspend fun getInfoBatch(userId: UserId?, targetIds: List<String>, targetType: VoteTargetType): Map<String, VoteInfo> =
        repo.getInfoBatch(userId, targetIds, targetType)
}
