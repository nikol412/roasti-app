package org.nikol.roasti.features.posts

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.datetime.timestamp
import org.nikol.roasti.features.recipes.RecipeTable
import org.nikol.roasti.features.users.UserTable
import kotlin.uuid.ExperimentalUuidApi

object PostTable : UuidTable(name = "posts") {
    @OptIn(ExperimentalUuidApi::class)
    val authorId = reference("author_id", UserTable.id, ReferenceOption.CASCADE)
    val text = text("text")
    val images = array<String>("images")
    @OptIn(ExperimentalUuidApi::class)
    val recipeId = reference("recipe_id", RecipeTable.id, ReferenceOption.SET_NULL)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

@OptIn(ExperimentalUuidApi::class)
fun ResultRow.toPost() = Post(
    id = this[PostTable.id].value,
    authorId = this[PostTable.authorId],
    text = this[PostTable.text],
    images = this[PostTable.images],
    recipeId = this[PostTable.recipeId].value,
    createdAt = this[PostTable.createdAt],
    updatedAt = this[PostTable.updatedAt]
)