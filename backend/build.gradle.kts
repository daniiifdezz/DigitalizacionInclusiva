plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    application
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    // ── Ktor Server ───────────────────────────────────────────────────────────
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.contentNeg)
    implementation(libs.ktor.server.callLogging)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.statusPages)
    implementation(libs.ktor.serialization.json)

    // ── Base de datos PostgreSQL ───────────────────────────────────────────────
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation("org.jetbrains.exposed:exposed-java-time:0.51.1")
    implementation(libs.postgresql)
    implementation(libs.hikariCP)

    // ── Serialización ─────────────────────────────────────────────────────────
    implementation(libs.kotlinx.serialization.json)

    // ── Logging ───────────────────────────────────────────────────────────────
    implementation(libs.logback.classic)

    // ── Testing ───────────────────────────────────────────────────────────────
    testImplementation(libs.kotlin.test)
    testImplementation(libs.ktor.server.testHost)
}

application {
    mainClass.set("org.dferna14.project.backend.ApplicationKt")
}
tasks.named<JavaExec>("run") {
    val envFile = rootProject.file(".env")
    if (envFile.exists()) {
        envFile.readLines()
            .filter { line ->
                line.isNotBlank() && !line.startsWith("#")
            }
            .forEach { line ->
                val index = line.indexOf('=')
                if (index != -1) {
                    val key   = line.substring(0, index).trim()
                    val value = line.substring(index + 1).trim()
                    environment(key, value)
                }
            }
    }
}
