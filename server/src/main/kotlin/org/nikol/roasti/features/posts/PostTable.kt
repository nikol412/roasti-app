package org.nikol.roasti.features.posts

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.datetime.timestamp
import org.nikol.roasti.features.users.UserTable
import org.nikol.roasti.features.users.UserId
import kotlin.uuid.ExperimentalUuidApi

object PostTable : UuidTable("posts") {
    val authorId = varchar("author_id", 128).references(UserTable.id, onDelete = ReferenceOption.CASCADE)
    val title = varchar("title", 500).nullable()
    val text = text("text").nullable()
    val images = array<String>("images")
    val recipeId = uuid("recipe_id").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

@OptIn(ExperimentalUuidApi::class)
internal data class PostRow(
    val id: PostId,
    val author: PostAuthor,
    val title: String?,
    val text: String?,
    val images: List<String>,
    val recipeId: kotlin.uuid.Uuid?,
    val createdAt: kotlin.time.Instant,
    val updatedAt: kotlin.time.Instant,
)

@OptIn(ExperimentalUuidApi::class)
internal fun ResultRow.toPostRow() = PostRow(
    id = PostId(this[PostTable.id].value),
    author = PostAuthor(
        id = UserId(this[UserTable.id]),
        username = this[UserTable.username],
        name = this[UserTable.name],
        avatarId = this[UserTable.avatarId],
    ),
    title = this[PostTable.title],
    text = this[PostTable.text],
    images = this[PostTable.images].toList(),
    recipeId = this[PostTable.recipeId],
    createdAt = this[PostTable.createdAt],
    updatedAt = this[PostTable.updatedAt],
)
