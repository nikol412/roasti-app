package org.nikol.roasti.features.recipes

import org.nikol.roasti.feature.recipe.domain.model.BrewMethod
import org.nikol.roasti.feature.recipe.domain.model.Difficulty
import org.nikol.roasti.feature.recipe.domain.model.RoastLevel
import org.nikol.roasti.features.comments.Comment
import org.nikol.roasti.features.comments.CommentId
import org.nikol.roasti.features.comments.CommentService
import org.nikol.roasti.features.comments.CommentTargetType
import org.nikol.roasti.features.comments.CommentThread
import org.nikol.roasti.common.domain.Page
import org.nikol.roasti.features.likes.LikeService
import org.nikol.roasti.features.likes.LikeTargetType
import org.nikol.roasti.features.likes.ToggleResult
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.nikol.roasti.features.users.UserId
import kotlin.uuid.ExperimentalUuidApi


interface RecipeService {
    suspend fun getById(id: RecipeId, userId: UserId?): Either<RecipeErrorCode, Recipe>
    suspend fun list(
        page: Int,
        limit: Int,
        authorId: UserId?,
        userId: UserId?,
        brewMethod: BrewMethod?,
        difficulty: Difficulty?,
        roastLevel: RoastLevel?,
    ): Page<Recipe>
    suspend fun create(userId: UserId, input: CreateRecipeInput): Either<RecipeErrorCode, Recipe>
    suspend fun update(userId: UserId, id: RecipeId, input: CreateRecipeInput): Either<RecipeErrorCode, Recipe>
    suspend fun delete(userId: UserId, id: RecipeId): Either<RecipeErrorCode, Unit>
    suspend fun toggleLike(userId: UserId, id: RecipeId): Either<RecipeErrorCode, ToggleResult>
    suspend fun listComments(id: RecipeId, page: Int, limit: Int): Either<RecipeErrorCode, Page<CommentThread>>
    suspend fun createComment(userId: UserId, id: RecipeId, text: String, parentId: CommentId?): Either<RecipeErrorCode, Comment>
    suspend fun clone(userId: UserId, id: RecipeId): Either<RecipeErrorCode, Recipe>
}

@OptIn(ExperimentalUuidApi::class)
class RecipeServiceImpl(
    private val repo: RecipeRepository,
    private val likeService: LikeService,
    private val commentService: CommentService,
) : RecipeService {

    override suspend fun getById(id: RecipeId, userId: UserId?): Either<RecipeErrorCode, Recipe> {
        val row = repo.findById(id) ?: return RecipeErrorCode.NOT_FOUND.left()
        if (!row.public && row.author.id != userId) return RecipeErrorCode.NOT_FOUND.left()
        val steps = repo.getSteps(id)
        return row.toRecipe(userId, steps).right()
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

    override suspend fun create(userId: UserId, input: CreateRecipeInput): Either<RecipeErrorCode, Recipe> {
        if (input.title.isBlank()) return RecipeErrorCode.INVALID_INPUT.left()
        if (input.description.isBlank()) return RecipeErrorCode.INVALID_INPUT.left()
        val row = repo.create(userId, input)
        val steps = repo.getSteps(row.id)
        return row.toRecipe(userId, steps).right()
    }

    override suspend fun update(userId: UserId, id: RecipeId, input: CreateRecipeInput): Either<RecipeErrorCode, Recipe> {
        if (input.title.isBlank()) return RecipeErrorCode.INVALID_INPUT.left()
        if (input.description.isBlank()) return RecipeErrorCode.INVALID_INPUT.left()
        val existing = repo.findById(id) ?: return RecipeErrorCode.NOT_FOUND.left()
        if (existing.author.id != userId) return RecipeErrorCode.FORBIDDEN.left()
        val row = repo.update(id, input) ?: return RecipeErrorCode.NOT_FOUND.left()
        val steps = repo.getSteps(id)
        return row.toRecipe(userId, steps).right()
    }

    override suspend fun delete(userId: UserId, id: RecipeId): Either<RecipeErrorCode, Unit> {
        val existing = repo.findById(id) ?: return RecipeErrorCode.NOT_FOUND.left()
        if (existing.author.id != userId) return RecipeErrorCode.FORBIDDEN.left()
        repo.delete(id)
        return Unit.right()
    }

    override suspend fun toggleLike(userId: UserId, id: RecipeId): Either<RecipeErrorCode, ToggleResult> {
        repo.findById(id) ?: return RecipeErrorCode.NOT_FOUND.left()
        return likeService.toggle(userId, id.value.toString(), LikeTargetType.RECIPE).right()
    }

    override suspend fun listComments(id: RecipeId, page: Int, limit: Int): Either<RecipeErrorCode, Page<CommentThread>> {
        repo.findById(id) ?: return RecipeErrorCode.NOT_FOUND.left()
        return commentService.list(id.value.toString(), CommentTargetType.RECIPE, page, limit).right()
    }

    override suspend fun createComment(
        userId: UserId,
        id: RecipeId,
        text: String,
        parentId: CommentId?,
    ): Either<RecipeErrorCode, Comment> {
        repo.findById(id) ?: return RecipeErrorCode.NOT_FOUND.left()
        return commentService.create(userId, id.value.toString(), CommentTargetType.RECIPE, text, parentId).right()
    }

    override suspend fun clone(userId: UserId, id: RecipeId): Either<RecipeErrorCode, Recipe> {
        val original = repo.findById(id) ?: return RecipeErrorCode.NOT_FOUND.left()
        if (!original.public && original.author.id != userId) return RecipeErrorCode.NOT_FOUND.left()
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
        return newRow.toRecipe(userId, steps).right()
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

