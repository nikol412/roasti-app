package org.nikol.roasti.features.recipes

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.datetime.timestamp
import org.nikol.roasti.feature.recipe.domain.model.BrewMethod
import org.nikol.roasti.feature.recipe.domain.model.Difficulty
import org.nikol.roasti.feature.recipe.domain.model.RoastLevel
import org.nikol.roasti.features.users.UserTable
import org.nikol.roasti.features.users.UserId
import kotlin.uuid.ExperimentalUuidApi

object RecipeTable : UuidTable("recipes") {
    val authorId = varchar("author_id", 128).references(UserTable.id, onDelete = ReferenceOption.CASCADE)
    val title = varchar("title", 255)
    val description = text("description")
    val note = text("note").nullable()
    val imageId = varchar("image_id", 255).nullable()
    val brewMethod = enumerationByName<BrewMethod>("brew_method", 50)
    val difficulty = enumerationByName<Difficulty>("difficulty", 50)
    val roastLevel = enumerationByName<RoastLevel>("roast_level", 50)
    val beans = varchar("beans", 255).nullable()
    val public = bool("public").default(true)
    @OptIn(ExperimentalUuidApi::class)
    val originRecipeId = uuid("origin_recipe_id").nullable()
    val originAuthorId = varchar("origin_author_id", 128).nullable()
    val originAuthorUsername = varchar("origin_author_username", 255).nullable()
    val originAuthorAvatarId = varchar("origin_author_avatar_id", 255).nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object BrewStepTable : IntIdTable("brew_steps") {
    @OptIn(ExperimentalUuidApi::class)
    val recipeId = reference("recipe_id", RecipeTable.id, onDelete = ReferenceOption.CASCADE)
    val title = varchar("title", 255)
    val description = text("description")
    val order = integer("order")
    val durationSeconds = integer("duration_seconds").nullable()
    val imageId = varchar("image_id", 255).nullable()
}

@OptIn(ExperimentalUuidApi::class)
data class RecipeRow(
    val id: RecipeId,
    val author: RecipeAuthor,
    val title: String,
    val description: String,
    val note: String?,
    val imageId: String?,
    val brewMethod: BrewMethod,
    val difficulty: Difficulty,
    val roastLevel: RoastLevel,
    val beans: String?,
    val public: Boolean,
    val originRecipeId: RecipeId?,
    val originAuthorId: String?,
    val originAuthorUsername: String?,
    val originAuthorAvatarId: String?,
    val createdAt: kotlin.time.Instant,
    val updatedAt: kotlin.time.Instant,
)

@OptIn(ExperimentalUuidApi::class)
fun ResultRow.toRecipeRow() = RecipeRow(
    id = RecipeId(this[RecipeTable.id].value),
    author = RecipeAuthor(
        id = UserId(this[UserTable.id]),
        username = this[UserTable.username],
        avatarId = this[UserTable.avatarId],
    ),
    title = this[RecipeTable.title],
    description = this[RecipeTable.description],
    note = this[RecipeTable.note],
    imageId = this[RecipeTable.imageId],
    brewMethod = this[RecipeTable.brewMethod],
    difficulty = this[RecipeTable.difficulty],
    roastLevel = this[RecipeTable.roastLevel],
    beans = this[RecipeTable.beans],
    public = this[RecipeTable.public],
    originRecipeId = this[RecipeTable.originRecipeId]?.let { RecipeId(it) },
    originAuthorId = this[RecipeTable.originAuthorId],
    originAuthorUsername = this[RecipeTable.originAuthorUsername],
    originAuthorAvatarId = this[RecipeTable.originAuthorAvatarId],
    createdAt = this[RecipeTable.createdAt],
    updatedAt = this[RecipeTable.updatedAt],
)

fun ResultRow.toBrewStep() = BrewStep(
    id = this[BrewStepTable.id].value,
    title = this[BrewStepTable.title],
    description = this[BrewStepTable.description],
    order = this[BrewStepTable.order],
    durationSeconds = this[BrewStepTable.durationSeconds],
    imageId = this[BrewStepTable.imageId],
)
