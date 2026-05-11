package org.nikol.roasti.features.likes

import org.nikol.roasti.features.users.UserId
import kotlin.uuid.Uuid

data class ToggleResult(val isLiked: Boolean, val count: Int)

interface LikeService {
    suspend fun toggle(userId: UserId, targetId: Uuid, targetType: LikeTargetType): ToggleResult
    suspend fun getInfo(userId: UserId?, targetId: Uuid, targetType: LikeTargetType): LikeInfo
    suspend fun getInfoBatch(userId: UserId?, targetIds: List<Uuid>, targetType: LikeTargetType): Map<Uuid, LikeInfo>
}

class LikeServiceImpl(private val repo: LikeRepository) : LikeService {

    override suspend fun toggle(userId: UserId, targetId: Uuid, targetType: LikeTargetType): ToggleResult {
        val exists = repo.exists(userId, targetId, targetType)
        if (exists) {
            repo.delete(userId, targetId, targetType)
        } else {
            repo.create(userId, targetId, targetType)
        }
        val info = repo.getInfo(userId, targetId, targetType)
        return ToggleResult(isLiked = !exists, count = info.count)
    }

    override suspend fun getInfo(userId: UserId?, targetId: Uuid, targetType: LikeTargetType): LikeInfo =
        repo.getInfo(userId, targetId, targetType)

    override suspend fun getInfoBatch(userId: UserId?, targetIds: List<Uuid>, targetType: LikeTargetType): Map<Uuid, LikeInfo> =
        repo.getInfoBatch(userId, targetIds, targetType)
}
