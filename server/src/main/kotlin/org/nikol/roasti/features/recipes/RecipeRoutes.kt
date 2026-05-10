package org.nikol.roasti.features.recipes

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import org.nikol.roasti.FIREBASE_AUTH
import org.nikol.roasti.FirebasePrincipal
import org.nikol.roasti.feature.comment.data.remote.model.request.CreateCommentRequestDto
import org.nikol.roasti.feature.comment.data.remote.model.response.CommentAuthorDto
import org.nikol.roasti.feature.comment.data.remote.model.response.CommentResponseDto
import org.nikol.roasti.feature.comment.data.remote.model.response.CommentThreadResponseDto
import org.nikol.roasti.core.network.PageResponseDto
import org.nikol.roasti.core.network.PaginationResponseDto
import org.nikol.roasti.features.common.Page
import org.nikol.roasti.feature.likes.data.RecipeLikeDto
import org.nikol.roasti.feature.recipe.data.remote.model.BrewMethodDto
import org.nikol.roasti.feature.recipe.data.remote.model.DifficultyDto
import org.nikol.roasti.feature.recipe.data.remote.model.RoastLevelDto
import org.nikol.roasti.feature.recipe.data.remote.model.request.CreateRecipeRequestDto
import org.nikol.roasti.feature.recipe.data.remote.model.response.AuthorResponseDto
import org.nikol.roasti.feature.recipe.data.remote.model.response.RecipeOriginResponseDto
import org.nikol.roasti.feature.recipe.data.remote.model.response.RecipeResponseDto
import org.nikol.roasti.feature.recipe.data.remote.model.response.RecipeStepResponseDto
import org.nikol.roasti.feature.recipe.domain.model.BrewMethod
import org.nikol.roasti.feature.recipe.domain.model.Difficulty
import org.nikol.roasti.feature.recipe.domain.model.RoastLevel
import org.nikol.roasti.features.comments.Comment
import org.nikol.roasti.features.comments.CommentAuthor
import org.nikol.roasti.features.comments.CommentId
import org.nikol.roasti.features.comments.CommentThread
import org.nikol.roasti.features.users.UserId
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun Route.recipeRoutes() {
    val recipeService by inject<RecipeService>()

    route("/recipes") {
        get {
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
            val authorId = call.request.queryParameters["author_id"]?.let { UserId(it) }
            val brewMethod = call.request.queryParameters["brew_method"]?.let { parseBrewMethodDto(it)?.toDomain() }
            val difficulty = call.request.queryParameters["difficulty"]?.let { parseDifficultyDto(it)?.toDomain() }
            val roastLevel = call.request.queryParameters["roast_level"]?.let { parseRoastLevelDto(it)?.toDomain() }
            val userId = call.principal<FirebasePrincipal>()?.uid
            val recipesPage = recipeService.list(page, limit, authorId, userId, brewMethod, difficulty, roastLevel)
            call.respond(recipesPage.toDto())
        }

        get("/{id}") {
            val id = call.parameters["id"]?.let { RecipeId(Uuid.parse(it)) }
                ?: return@get call.respond(HttpStatusCode.BadRequest)
            val userId = call.principal<FirebasePrincipal>()?.uid
            val recipe = recipeService.getById(id, userId)
            call.respond(recipe.toDto())
        }

        get("/{id}/comments") {
            val id = call.parameters["id"]?.let { RecipeId(Uuid.parse(it)) }
                ?: return@get call.respond(HttpStatusCode.BadRequest)
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
            val result = recipeService.listComments(id, page, limit)
            call.respond(
                PageResponseDto(
                    items = result.items.map { it.toDto() },
                    pagination = PaginationResponseDto(
                        currentPage = result.currentPage,
                        itemsCount = result.itemsCount,
                        lastPage = result.lastPage,
                        nextPage = result.nextPage,
                    ),
                )
            )
        }

        authenticate(FIREBASE_AUTH) {
            post {
                val userId = call.principal<FirebasePrincipal>()!!.uid
                val body = call.receive<CreateRecipeRequestDto>()
                val recipe = recipeService.create(userId, body.toInput())
                call.respond(HttpStatusCode.Created, recipe.toDto())
            }

            put("/{id}") {
                val id = call.parameters["id"]?.let { RecipeId(Uuid.parse(it)) }
                    ?: return@put call.respond(HttpStatusCode.BadRequest)
                val userId = call.principal<FirebasePrincipal>()!!.uid
                val body = call.receive<CreateRecipeRequestDto>()
                val recipe = recipeService.update(userId, id, body.toInput())
                call.respond(recipe.toDto())
            }

            delete("/{id}") {
                val id = call.parameters["id"]?.let { RecipeId(Uuid.parse(it)) }
                    ?: return@delete call.respond(HttpStatusCode.BadRequest)
                val userId = call.principal<FirebasePrincipal>()!!.uid
                recipeService.delete(userId, id)
                call.respond(HttpStatusCode.NoContent)
            }

            post("/{id}/like") {
                val id = call.parameters["id"]?.let { RecipeId(Uuid.parse(it)) }
                    ?: return@post call.respond(HttpStatusCode.BadRequest)
                val userId = call.principal<FirebasePrincipal>()!!.uid
                val result = recipeService.toggleLike(userId, id)
                call.respond(RecipeLikeDto(isLiked = result.isLiked, likesCount = result.count))
            }

            post("/{id}/comments") {
                val id = call.parameters["id"]?.let { RecipeId(Uuid.parse(it)) }
                    ?: return@post call.respond(HttpStatusCode.BadRequest)
                val userId = call.principal<FirebasePrincipal>()!!.uid
                val body = call.receive<CreateCommentRequestDto>()
                val parentId = body.parentId?.let { CommentId(Uuid.parse(it)) }
                val comment = recipeService.createComment(userId, id, body.text, parentId)
                call.respond(HttpStatusCode.Created, comment.toDto())
            }

            post("/{id}/clone") {
                val id = call.parameters["id"]?.let { RecipeId(Uuid.parse(it)) }
                    ?: return@post call.respond(HttpStatusCode.BadRequest)
                val userId = call.principal<FirebasePrincipal>()!!.uid
                val recipe = recipeService.clone(userId, id)
                call.respond(HttpStatusCode.Created, recipe.toDto())
            }
        }
    }
}

private fun CreateRecipeRequestDto.toInput() = CreateRecipeInput(
    title = title,
    description = description,
    note = note,
    imageId = imageId,
    brewMethod = brewMethod.toDomain(),
    difficulty = difficulty.toDomain(),
    roastLevel = roastLevel.toDomain(),
    beans = beans,
    public = true,
    steps = steps.map { step ->
        CreateBrewStepInput(
            title = step.title,
            description = step.description,
            order = step.order,
            durationSeconds = step.durationSeconds,
            imageId = step.imageId,
        )
    },
)

@OptIn(ExperimentalUuidApi::class)
private fun Recipe.toDto() = RecipeResponseDto(
    id = id.value.toString(),
    authorId = author.id.value,
    author = AuthorResponseDto(
        id = author.id.value,
        username = author.username,
        avatarId = author.avatarId,
    ),
    title = title,
    description = description,
    note = note,
    imageId = imageId,
    brewMethod = brewMethod.toDto(),
    difficulty = difficulty.toDto(),
    roastLevel = roastLevel.toDto(),
    beans = beans,
    steps = steps.map { it.toDto() },
    isLiked = isLiked,
    likesCount = likesCount,
    origin = origin?.toDto(),
    isPublic = public,
    createdAt = kotlinx.datetime.Instant.fromEpochMilliseconds(createdAt.toEpochMilliseconds()),
    updatedAt = kotlinx.datetime.Instant.fromEpochMilliseconds(updatedAt.toEpochMilliseconds()),
)

@OptIn(ExperimentalUuidApi::class)
private fun Page<Recipe>.toDto() = PageResponseDto(
    items = items.map { it.toDto() },
    pagination = PaginationResponseDto(
        currentPage = currentPage,
        itemsCount = itemsCount,
        lastPage = lastPage,
        nextPage = nextPage,
    ),
)

private fun BrewStep.toDto() = RecipeStepResponseDto(
    order = order,
    title = title,
    description = description,
    durationSeconds = durationSeconds,
    imageId = imageId,
)

@OptIn(ExperimentalUuidApi::class)
private fun RecipeOriginInfo.toDto() = RecipeOriginResponseDto(
    author = AuthorResponseDto(
        id = author.id.value,
        username = author.username,
        avatarId = author.avatarId,
    ),
    recipeId = recipeId.value.toString(),
)

private fun BrewMethod.toDto() = when (this) {
    BrewMethod.V60 -> BrewMethodDto.V60
    BrewMethod.FrenchPress -> BrewMethodDto.FRENCH_PRESS
    BrewMethod.Aeropress -> BrewMethodDto.AEROPRESS
    BrewMethod.Chemex -> BrewMethodDto.CHEMEX
    BrewMethod.ColdBrew -> BrewMethodDto.COLD_BREW
    BrewMethod.EspressoMachine -> BrewMethodDto.EXPRESSO_MACHINE
    BrewMethod.MokaPot -> BrewMethodDto.MOKA_POT
    BrewMethod.NONE -> BrewMethodDto.NONE
}

private fun Difficulty.toDto() = when (this) {
    Difficulty.Easy -> DifficultyDto.EASY
    Difficulty.Medium -> DifficultyDto.MEDIUM
    Difficulty.Hard -> DifficultyDto.HARD
}

private fun RoastLevel.toDto() = when (this) {
    RoastLevel.Light -> RoastLevelDto.LIGHT
    RoastLevel.MediumLight -> RoastLevelDto.MEDIUM_LIGHT
    RoastLevel.Medium -> RoastLevelDto.MEDIUM
    RoastLevel.MediumDark -> RoastLevelDto.MEDIUM_DARK
    RoastLevel.Dark -> RoastLevelDto.DARK
    RoastLevel.NONE -> RoastLevelDto.NONE
}

private fun BrewMethodDto?.toDomain() = when (this) {
    BrewMethodDto.V60 -> BrewMethod.V60
    BrewMethodDto.FRENCH_PRESS -> BrewMethod.FrenchPress
    BrewMethodDto.AEROPRESS -> BrewMethod.Aeropress
    BrewMethodDto.CHEMEX -> BrewMethod.Chemex
    BrewMethodDto.COLD_BREW -> BrewMethod.ColdBrew
    BrewMethodDto.EXPRESSO_MACHINE -> BrewMethod.EspressoMachine
    BrewMethodDto.MOKA_POT -> BrewMethod.MokaPot
    BrewMethodDto.NONE, null -> BrewMethod.NONE
}

private fun DifficultyDto.toDomain() = when (this) {
    DifficultyDto.EASY -> Difficulty.Easy
    DifficultyDto.MEDIUM -> Difficulty.Medium
    DifficultyDto.HARD -> Difficulty.Hard
}

private fun RoastLevelDto?.toDomain() = when (this) {
    RoastLevelDto.LIGHT -> RoastLevel.Light
    RoastLevelDto.MEDIUM_LIGHT -> RoastLevel.MediumLight
    RoastLevelDto.MEDIUM -> RoastLevel.Medium
    RoastLevelDto.MEDIUM_DARK -> RoastLevel.MediumDark
    RoastLevelDto.DARK -> RoastLevel.Dark
    RoastLevelDto.NONE, null -> RoastLevel.NONE
}

private fun parseBrewMethodDto(value: String) = runCatching {
    BrewMethodDto.valueOf(value.uppercase())
}.getOrNull()

private fun parseDifficultyDto(value: String) = runCatching {
    DifficultyDto.valueOf(value.uppercase())
}.getOrNull()

private fun parseRoastLevelDto(value: String) = runCatching {
    RoastLevelDto.valueOf(value.uppercase())
}.getOrNull()

private fun CommentAuthor.toDto() = CommentAuthorDto(
    id = id.value,
    username = username,
    name = name,
    avatarId = avatarId,
)

@OptIn(ExperimentalUuidApi::class)
private fun Comment.toDto() = CommentResponseDto(
    id = id.value.toString(),
    isDeleted = isDeleted,
    author = author?.toDto(),
    text = text,
    parentId = parentId?.value?.toString(),
    createdAt = kotlinx.datetime.Instant.fromEpochMilliseconds(createdAt.toEpochMilliseconds()),
    updatedAt = kotlinx.datetime.Instant.fromEpochMilliseconds(updatedAt.toEpochMilliseconds()),
)

private fun CommentThread.toDto() = root.toDto().let { r ->
    CommentThreadResponseDto(
        id = r.id,
        isDeleted = r.isDeleted,
        author = r.author,
        text = r.text,
        parentId = r.parentId,
        replies = replies.map { it.toDto() },
        createdAt = r.createdAt,
        updatedAt = r.updatedAt,
    )
}
