package org.nikol.roasti.features.recipes

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.ResultRow
import org.nikol.roasti.feature.recipe.domain.model.BrewMethod
import org.nikol.roasti.feature.recipe.domain.model.Difficulty
import org.nikol.roasti.feature.recipe.domain.model.RoastLevel
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.datetime.timestamp
import org.nikol.roasti.features.posts.Post
import org.nikol.roasti.features.posts.PostTable
import org.nikol.roasti.features.users.UserTable
import kotlin.uuid.ExperimentalUuidApi


object RecipeTable : UuidTable(name = "recipes") {
    @OptIn(ExperimentalUuidApi::class)
    val authorId = reference("author_id", UserTable.id, ReferenceOption.CASCADE)
    val title = varchar("title", 255)
    val description = text("description")
    val imageUrl = varchar("image_url", 255)
    val brewMethod = enumerationByName<BrewMethod>("brew_method", 50)
    val difficulty = enumerationByName<Difficulty>("difficulty", 50)
    val roastLevel = enumerationByName<RoastLevel>("roast_level", 50)
    val public = bool("public")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

@OptIn(ExperimentalUuidApi::class)
fun ResultRow.toRecipe(steps: List<BrewStep>) = Recipe(
    id = this[RecipeTable.id].value,
    title = this[RecipeTable.title],
    description = this[RecipeTable.description],
    imageUrl = this[RecipeTable.imageUrl],
    brewMethod = this[RecipeTable.brewMethod],
    difficulty = this[RecipeTable.difficulty],
    roastLevel = this[RecipeTable.roastLevel],
    public = this[RecipeTable.public],
    steps = steps,
    createdAt = this[RecipeTable.createdAt],
    updatedAt = this[RecipeTable.updatedAt]
)