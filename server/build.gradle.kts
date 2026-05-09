plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinSerialization)
    application
}

application {
    mainClass.set("org.nikol.roasti.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.koin.ktor)
    implementation(libs.logback)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.contentNegotiation)
    implementation(libs.ktor.server.resources)
    implementation(libs.ktor.server.statusPages)
    implementation(libs.ktor.server.callLogging)
    implementation(libs.ktor.server.callId)
    implementation(libs.arrow.core)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlinDatetime)
    implementation(libs.h2)
    implementation(libs.firebase.admin)
    implementation(libs.ktor.server.auth)
    testImplementation(libs.ktor.server.testHost)
    testImplementation(libs.kotlin.testJunit)
}

kotlin {
    jvmToolchain(21)
}

val firebaseEmulatorEnv = mapOf(
    "FIREBASE_AUTH_EMULATOR_HOST" to "localhost:9099",
    "FIREBASE_API_KEY" to "test",
    "FIREBASE_PROJECT_ID" to "roasti-dev-project",
    "FIREBASE_IDENTITY_BASE_URL" to "http://localhost:9099/identitytoolkit.googleapis.com/v1/accounts",
    "FIREBASE_TOKEN_BASE_URL" to "http://localhost:9099/securetoken.googleapis.com/v1/token",
)

tasks.register<Exec>("firebaseEmulator") {
    workingDir = rootProject.projectDir
    commandLine(
        "sh", "-c",
        "firebase emulators:start --only auth --project roasti-dev-project",
    )
}

tasks.named<JavaExec>("run") {
    environment(firebaseEmulatorEnv)
}

tasks.register("serverDev") {
    dependsOn("run")
}
