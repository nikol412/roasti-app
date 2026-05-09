package org.nikol.roasti

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.bearer
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.resources.Resources
import io.ktor.server.response.respond
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.nikol.roasti.features.auth.AuthService
import org.nikol.roasti.features.auth.AuthServiceImpl
import org.nikol.roasti.features.auth.FirebaseSigner
import org.nikol.roasti.features.auth.FirebaseSignerImpl
import org.nikol.roasti.features.auth.RevokedTokenRepository
import org.nikol.roasti.features.auth.RevokedTokenRepositoryImpl
import org.nikol.roasti.features.auth.RevokedTokenTable
import org.nikol.roasti.features.auth.authRoutes
import org.nikol.roasti.features.users.UserRepository
import org.nikol.roasti.features.users.UserRepositoryImpl
import org.nikol.roasti.features.users.UserService
import org.nikol.roasti.features.users.UserServiceImpl
import org.nikol.roasti.features.users.UserId
import org.nikol.roasti.features.users.UserTable
import org.nikol.roasti.features.users.userRoutes
import org.nikol.roasti.features.comments.CommentRepository
import org.nikol.roasti.features.comments.CommentRepositoryImpl
import org.nikol.roasti.features.comments.CommentService
import org.nikol.roasti.features.comments.CommentServiceImpl
import org.nikol.roasti.features.comments.CommentTable
import org.nikol.roasti.features.likes.LikeRepository
import org.nikol.roasti.features.likes.LikeRepositoryImpl
import org.nikol.roasti.features.likes.LikeService
import org.nikol.roasti.features.likes.LikeServiceImpl
import org.nikol.roasti.features.likes.LikeTable
import org.nikol.roasti.features.votes.VoteRepository
import org.nikol.roasti.features.votes.VoteRepositoryImpl
import org.nikol.roasti.features.votes.VoteService
import org.nikol.roasti.features.votes.VoteServiceImpl
import org.nikol.roasti.features.votes.VoteTable
import org.nikol.roasti.features.posts.PostRepository
import org.nikol.roasti.features.posts.PostRepositoryImpl
import org.nikol.roasti.features.posts.PostService
import org.nikol.roasti.features.posts.PostServiceImpl
import org.nikol.roasti.features.posts.PostTable
import org.nikol.roasti.features.posts.postRoutes
import org.nikol.roasti.features.recipes.BrewStepTable
import org.nikol.roasti.features.recipes.RecipeRepository
import org.nikol.roasti.features.recipes.RecipeRepositoryImpl
import org.nikol.roasti.features.recipes.RecipeService
import org.nikol.roasti.features.recipes.RecipeServiceImpl
import org.nikol.roasti.features.recipes.RecipeTable
import org.nikol.roasti.features.recipes.recipeRoutes
import org.slf4j.event.Level
import java.io.ByteArrayInputStream
import java.util.Base64
import java.util.UUID

fun main(args: Array<String>) =
    io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    val dbUrl = environment.config.property("database.url").getString()
    val dbDriver = environment.config.property("database.driver").getString()

    Database.connect(url = dbUrl, driver = dbDriver)
    transaction { SchemaUtils.create(UserTable, RevokedTokenTable, VoteTable, LikeTable, CommentTable, PostTable, RecipeTable, BrewStepTable) }

    initFirebase()

    val firebaseApiKey = environment.config.property("firebase.apiKey").getString()
    val identityBaseUrl = environment.config.propertyOrNull("firebase.identityBaseUrl")?.getString()
        ?: "https://identitytoolkit.googleapis.com/v1/accounts"
    val tokenBaseUrl = environment.config.propertyOrNull("firebase.tokenBaseUrl")?.getString()
        ?: "https://securetoken.googleapis.com/v1/token"

    install(Koin) {
        modules(module {
            single<UserRepository> { UserRepositoryImpl() }
            single<UserService> { UserServiceImpl(get()) }
            single<FirebaseSigner> { FirebaseSignerImpl(firebaseApiKey, identityBaseUrl, tokenBaseUrl) }
            single<RevokedTokenRepository> { RevokedTokenRepositoryImpl() }
            single { FirebaseAuth.getInstance() }
            single<CommentRepository> { CommentRepositoryImpl() }
            single<CommentService> { CommentServiceImpl(get()) }
            single<PostRepository> { PostRepositoryImpl() }
            single<PostService> { PostServiceImpl(get(), get(), get()) }
            single<LikeRepository> { LikeRepositoryImpl() }
            single<LikeService> { LikeServiceImpl(get()) }
            single<VoteRepository> { VoteRepositoryImpl() }
            single<VoteService> { VoteServiceImpl(get()) }
            single<RecipeRepository> { RecipeRepositoryImpl() }
            single<RecipeService> { RecipeServiceImpl(get(), get(), get()) }
            single<AuthService> { AuthServiceImpl(get(), get(), get(), get(), get()) }
        })
    }

    val firebaseAuth = FirebaseAuth.getInstance()
    install(Authentication) {
        bearer(FIREBASE_AUTH) {
            authenticate { credential ->
                try {
                    val decoded = firebaseAuth.verifyIdToken(credential.token)
                    FirebasePrincipal(UserId(decoded.uid))
                } catch (e: FirebaseAuthException) {
                    null
                }
            }
        }
    }

    install(CallId) {
        generate { UUID.randomUUID().toString() }
    }
    install(CallLogging) {
        level = Level.INFO
        callIdMdc("requestId")
        format { call ->
            "${call.request.httpMethod.value} ${call.request.path()} -> ${call.response.status()}"
        }
    }
    install(Resources)
    install(ContentNegotiation) {
        json(kotlinx.serialization.json.Json { explicitNulls = false })
    }
    install(StatusPages) {
        exception<io.ktor.server.plugins.BadRequestException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest)
        }
        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.UnprocessableEntity, cause.message ?: "validation error")
        }
        exception<IllegalStateException> { call, cause ->
            call.respond(HttpStatusCode.Conflict, cause.message ?: "conflict")
        }
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
    routing {
        route("/api/v1") {
            authRoutes()
            userRoutes()
            postRoutes()
            recipeRoutes()
        }
    }
}

private fun Application.initFirebase() {
    if (FirebaseApp.getApps().isNotEmpty()) return

    val credsBase64 = environment.config.propertyOrNull("firebase.credentialsBase64")?.getString()
    val isEmulator = System.getenv("FIREBASE_AUTH_EMULATOR_HOST") != null

    val credentials = when {
        !credsBase64.isNullOrBlank() -> GoogleCredentials.fromStream(ByteArrayInputStream(Base64.getDecoder().decode(credsBase64)))
        isEmulator -> GoogleCredentials.newBuilder().build()
        else -> GoogleCredentials.getApplicationDefault()
    }

    val projectId = environment.config.propertyOrNull("firebase.projectId")?.getString()

    FirebaseApp.initializeApp(
        FirebaseOptions.builder()
            .setCredentials(credentials)
            .apply { if (projectId != null) setProjectId(projectId) }
            .build()
    )
}
