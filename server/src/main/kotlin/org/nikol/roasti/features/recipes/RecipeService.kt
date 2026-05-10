package org.nikol.roasti.features.recipes

import org.nikol.roasti.feature.recipe.domain.model.BrewMethod
import org.nikol.roasti.feature.recipe.domain.model.Difficulty
import org.nikol.roasti.feature.recipe.domain.model.RoastLevel
import org.nikol.roasti.features.comments.Comment
import org.nikol.roasti.features.comments.CommentId
import org.nikol.roasti.features.comments.CommentService
import org.nikol.roasti.features.comments.CommentThread
import org.nikol.roasti.features.common.Page
import org.nikol.roasti.features.likes.LikeService
import org.nikol.roasti.features.likes.LikeTargetType
import org.nikol.roasti.features.likes.ToggleResult
import org.nikol.roasti.features.users.UserId
import kotlin.uuid.ExperimentalUuidApi

private const val COMMENT_TARGET_TYPE = "recipe"

interface RecipeService {
    suspend fun getById(id: RecipeId, userId: UserId?): Recipe
    suspend fun list(
        page: Int,
        limit: Int,
        authorId: UserId?,
        userId: UserId?,
        brewMethod: BrewMethod?,
        difficulty: Difficulty?,
        roastLevel: RoastLevel?,
    ): Page<Recipe>
    suspend fun create(userId: UserId, input: CreateRecipeInput): Recipe
    suspend fun update(userId: UserId, id: RecipeId, input: CreateRecipeInput): Recipe
    suspend fun delete(userId: UserId, id: RecipeId)
    suspend fun toggleLike(userId: UserId, id: RecipeId): ToggleResult
    suspend fun listComments(id: RecipeId, page: Int, limit: Int): Page<CommentThread>
    suspend fun createComment(userId: UserId, id: RecipeId, text: String, parentId: CommentId?): Comment
    suspend fun clone(userId: UserId, id: RecipeId): Recipe
}

@OptIn(ExperimentalUuidApi::class)
class RecipeServiceImpl(
    private val repo: RecipeRepository,
    private val likeService: LikeService,
    private val commentService: CommentService,
) : RecipeService {

    override suspend fun getById(id: RecipeId, userId: UserId?): Recipe {
        val row = repo.findById(id) ?: throw RecipeNotFoundException
        if (!row.public && row.author.id != userId) throw RecipeNotFoundException
        val steps = repo.getSteps(id)
        return row.toRecipe(userId, steps)
    }

    override suspend fun list(
        page: Int,
        limit: Int,
        authorId: UserId?,
        userId: UserId?,
        brewMethod: BrewMethod?,
        difficulty: Difficulty?,
        roastLevel: RoastLevel?,
    ): Page<Recipe> {
        val (rows, total) = repo.list(page, limit, authorId, userId, brewMethod, difficulty, roastLevel)
        val recipeIds = rows.map { it.id }
        val stepsMap = repo.getStepsBatch(recipeIds)
        val targetIds = recipeIds.map { it.value.toString() }
        val likeInfos = likeService.getInfoBatch(userId, targetIds, LikeTargetType.RECIPE)

        val recipes = rows.map { row ->
            val id = row.id.value.toString()
            val likeInfo = likeInfos[id]
            row.toRecipeWithLikes(
                steps = stepsMap[row.id] ?: emptyList(),
                isLiked = likeInfo?.isLiked ?: false,
                likesCount = likeInfo?.count ?: 0,
            )
        }

        return Page.of(recipes, page, total, limit)
    }

    override suspend fun create(userId: UserId, input: CreateRecipeInput): Recipe {
        val row = repo.create(userId, input)
        val steps = repo.getSteps(row.id)
        return row.toRecipe(userId, steps)
    }

    override suspend fun update(userId: UserId, id: RecipeId, input: CreateRecipeInput): Recipe {
        val existing = repo.findById(id) ?: throw RecipeNotFoundException
        if (existing.author.id != userId) throw RecipeForbiddenException
        val row = repo.update(id, input) ?: throw RecipeNotFoundException
        val steps = repo.getSteps(id)
        return row.toRecipe(userId, steps)
    }

    override suspend fun delete(userId: UserId, id: RecipeId) {
        val existing = repo.findById(id) ?: throw RecipeNotFoundException
        if (existing.author.id != userId) throw RecipeForbiddenException
        repo.delete(id)
    }

    override suspend fun toggleLike(userId: UserId, id: RecipeId): ToggleResult {
        repo.findById(id) ?: throw RecipeNotFoundException
        return likeService.toggle(userId, id.value.toString(), LikeTargetType.RECIPE)
    }

    override suspend fun listComments(id: RecipeId, page: Int, limit: Int): Page<CommentThread> {
        repo.findById(id) ?: throw RecipeNotFoundException
        return commentService.list(id.value.toString(), COMMENT_TARGET_TYPE, page, limit)
    }

    override suspend fun createComment(
        userId: UserId,
        id: RecipeId,
        text: String,
        parentId: CommentId?,
    ): Comment {
        repo.findById(id) ?: throw RecipeNotFoundException
        return commentService.create(userId, id.value.toString(), COMMENT_TARGET_TYPE, text, parentId)
    }

    override suspend fun clone(userId: UserId, id: RecipeId): Recipe {
        val original = repo.findById(id) ?: throw RecipeNotFoundException
        if (!original.public && original.author.id != userId) throw RecipeNotFoundException
        val originalSteps = repo.getSteps(id)
        val newRow = repo.create(
            userId,
            CreateRecipeInput(
                title = original.title,
                description = original.description,
                note = original.note,
                imageId = original.imageId,
                brewMethod = original.brewMethod,
                difficulty = original.difficulty,
                roastLevel = original.roastLevel,
                beans = original.beans,
                public = false,
                steps = originalSteps.map {
                    CreateBrewStepInput(
                        title = it.title,
                        description = it.description,
                        order = it.order,
                        durationSeconds = it.durationSeconds,
                        imageId = it.imageId,
                    )
                },
                originRecipeId = id,
            ),
        )
        val steps = repo.getSteps(newRow.id)
        return newRow.toRecipe(userId, steps)
    }

    private suspend fun RecipeRow.toRecipe(userId: UserId?, steps: List<BrewStep>): Recipe {
        val likeInfo = likeService.getInfo(userId, id.value.toString(), LikeTargetType.RECIPE)
        return toRecipeWithLikes(steps, likeInfo.isLiked, likeInfo.count)
    }

    private fun RecipeRow.toRecipeWithLikes(
        steps: List<BrewStep>,
        isLiked: Boolean,
        likesCount: Int,
    ) = Recipe(
        id = id,
        author = author,
        title = title,
        description = description,
        note = note,
        imageId = imageId,
        brewMethod = brewMethod,
        difficulty = difficulty,
        roastLevel = roastLevel,
        beans = beans,
        public = public,
        steps = steps,
        isLiked = isLiked,
        likesCount = likesCount,
        origin = origin,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

}

val RecipeNotFoundException = NoSuchElementException("recipe not found")
val RecipeForbiddenException = IllegalStateException("forbidden")
