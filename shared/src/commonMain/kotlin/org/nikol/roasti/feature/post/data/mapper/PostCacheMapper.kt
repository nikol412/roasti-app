package org.nikol.roasti.feature.post.data.mapper

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.nikol.roasti.RoastiDatabaseCache
import org.nikol.roasti.feature.post.data.remote.model.response.PostResponseDto

private val photosJson = Json { ignoreUnknownKeys = true }
private val photosSerializer = ListSerializer(String.serializer())

fun List<String>.encodePhotos(): String =
    photosJson.encodeToString(photosSerializer, this)

fun String.parsePhotos(): List<String> =
    if (isBlank()) emptyList() else photosJson.decodeFromString(photosSerializer, this)

fun RoastiDatabaseCache.upsertPost(dto: PostResponseDto) {
    postQueries.insertPost(
        id = dto.id,
        text = dto.text,
        photos_json = dto.photos.encodePhotos(),
        recipe_id = dto.recipeId,
        rating = dto.rating.toLong(),
        user_reaction = dto.userReaction,
        comments_count = dto.commentsCount.toLong(),
        author_id = dto.author.id,
        author_name = dto.author.username,
        author_image_id = dto.author.avatarId,
        created_at = dto.createdAt.toString(),
        updated_at = dto.updatedAt.toString(),
    )
}
